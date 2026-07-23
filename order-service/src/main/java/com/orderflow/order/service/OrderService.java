package com.orderflow.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.order.client.InventoryClient;
import com.orderflow.order.dto.inventory.StockReserveItem;
import com.orderflow.order.dto.inventory.StockReserveRequest;
import com.orderflow.order.dto.order.*;
import com.orderflow.order.enums.OrderStatus;
import com.orderflow.order.exceptions.BadRequestException;
import com.orderflow.order.exceptions.DuplicateIdempotencyKeyException;
import com.orderflow.order.exceptions.PaymentServiceUnavailableException;
import com.orderflow.order.exceptions.ResourceNotFound;
import com.orderflow.order.model.Order;
import com.orderflow.order.model.Product;
import com.orderflow.order.repository.OrderRepository;
import com.orderflow.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderPersistenceService orderPersistenceService;
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    //private final StripeService stripeService;

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    public OrderResponse createOrder(
            CreateOrderRequest request,
            Long userId,
            String idempotencyKey
    ) {
        String cacheKey = "idempotency:" + idempotencyKey;
        OrderResponse cachedResponse = getCachedResponse(cacheKey, userId);
        if (cachedResponse != null) return cachedResponse;

        Map<Long, Product> products = validateAndLoadProducts(request);
        StockReserveRequest reserveRequest = buildReserveRequest(request);
        reserveStock(reserveRequest);

        try {
            Order order = orderPersistenceService.saveOrder(request, products, userId);
            String clientSecret = createPaymentIntent(order, reserveRequest);
            log.info("Order created [orderId={}, userId={}, total={}]",order.getId(), userId, order.getTotalAmount());

            publishOrderCreated(order);

            return cacheResponse(cacheKey, userId, order, clientSecret);
        } catch (PaymentServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Order creation failed, releasing stock [userId={}]", userId, e);
            inventoryClient.releaseStock(reserveRequest);
            throw e;
        }
    }

    private OrderResponse getCachedResponse(String cacheKey, Long userId) {
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached == null) return null;

        IdempotencyRecord record = deserializeRecord(cached);
        if (!record.userId().equals(userId)) {
            throw new DuplicateIdempotencyKeyException(
                    "Idempotency key already used by another user"
            );
        }

        log.info("Idempotent request [key={}]", cacheKey.replace("idempotency:", ""));
        return record.response();
    }

    private Map<Long, Product> validateAndLoadProducts(CreateOrderRequest request) {

        List<Long> productIds = request.items().stream()
                .map(OrderItemRequest::productId)
                .toList();

        if (productIds.size() != productIds.stream().distinct().count()) {
            throw new BadRequestException("Duplicate products in order");
        }

        Map<Long, Product> products = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        if (products.size() != productIds.size()) {
            throw new ResourceNotFound("One or more products not found");
        }

        return products;
    }

    private StockReserveRequest buildReserveRequest(CreateOrderRequest request) {
        return new StockReserveRequest(
                request.items().stream()
                        .map(item -> new StockReserveItem(item.productId(), item.quantity()))
                        .toList()
        );
    }

    private void reserveStock(StockReserveRequest reserveRequest) {
        inventoryClient.reserveStock(reserveRequest);
    }

    private String createPaymentIntent(
            Order order,
            StockReserveRequest reserveRequest
    ) {
        try {
            // String clientSecret = stripeService.createPaymentIntent(order.getTotalAmount());
            String clientSecret = "pi_test_secret";

            // order.setStripePaymentIntentId(stripeService.extractPaymentIntentId(clientSecret));
            order.setStripePaymentIntentId(clientSecret);

            orderRepository.save(order);

            return clientSecret;

        } catch (Exception e) {
            log.error("Stripe PaymentIntent creation failed, rolling back [orderId={}]",
                    order.getId(), e);

            inventoryClient.releaseStock(reserveRequest);
            orderRepository.delete(order);

            throw new PaymentServiceUnavailableException("Payment service unavailable");
        }
    }

    private void publishOrderCreated(Order order) {
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
    }

    private OrderResponse cacheResponse(
            String cacheKey,
            Long userId,
            Order order,
            String clientSecret
    ) {
        OrderResponse response = OrderResponse.toDto(order, clientSecret);

        IdempotencyRecord record = new IdempotencyRecord(userId, response);

        redisTemplate.opsForValue().set(
                cacheKey,
                serializeRecord(record),
                IDEMPOTENCY_TTL
        );

        return response;
    }

    @Transactional
    public void handlePaymentSucceeded(String paymentIntentId) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFound("Order not found for paymentIntentId: " + paymentIntentId));

        switch (order.getStatus()) {
            case PENDING -> {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);

                inventoryClient.confirmStock(toStockRequest(order));

                log.info("Order completed [orderId={}]", order.getId());
                eventPublisher.publishEvent(new OrderCompletedEvent(order));
            }
            case EXPIRED -> {
                log.warn("Payment succeeded for an already expired order — refunding [orderId={}]", order.getId());
                //stripeService.refundPaymentIntent(paymentIntentId);
            }
            case COMPLETED -> log.info("Order already completed, ignoring duplicate webhook [orderId={}]", order.getId());
            case PAYMENT_FAILED -> log.warn("Payment succeeded for an order marked as failed - refunding [orderId={}]", order.getId());
            default -> log.warn("Unhandled order status on payment success [orderId={}, status={}]", order.getId(), order.getStatus());
        }
    }

    @Transactional
    public void handlePaymentFailed(String paymentIntentId) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFound("Order not found for paymentIntentId: " + paymentIntentId));

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        inventoryClient.releaseStock(toStockRequest(order));

        log.info("Payment failed [orderId={}]", order.getId());
    }

    @Transactional
    public void expireOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("Order not PENDING, ignoring expiry [orderId={}, status={}]", order.getId(), order.getStatus());
            return;
        }

        // Status primeiro, commit, só depois os side effects
        order.setStatus(OrderStatus.EXPIRED);
        orderRepository.save(order);

        //stripeService.cancelPaymentIntent(order.getStripePaymentIntentId());
        inventoryClient.releaseStock(toStockRequest(order));

        log.info("Order expired [orderId={}]", order.getId());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Order not found"));

        return OrderResponse.toDto(order, null);
    }

    private StockReserveRequest toStockRequest(Order order) {
        return new StockReserveRequest(
                order.getItems().stream()
                        .map(i -> new StockReserveItem(i.getProductId(), i.getQuantity()))
                        .toList()
        );
    }

    private record IdempotencyRecord(Long userId, OrderResponse response) {}

    private String serializeRecord(IdempotencyRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize idempotency record", e);
        }
    }

    private IdempotencyRecord deserializeRecord(String json) {
        try {
            return objectMapper.readValue(json, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize idempotency record", e);
        }
    }
}
