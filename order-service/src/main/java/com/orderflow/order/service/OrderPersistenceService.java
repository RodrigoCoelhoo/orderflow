package com.orderflow.order.service;

import com.orderflow.order.config.OrderConstants;
import com.orderflow.order.dto.order.CreateOrderRequest;
import com.orderflow.order.dto.order.OrderItemRequest;
import com.orderflow.order.model.Order;
import com.orderflow.order.model.OrderItem;
import com.orderflow.order.model.Product;
import com.orderflow.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order saveOrder(
            CreateOrderRequest request,
            Map<Long, Product> products,
            Long userId
    ) {

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = products.get(itemRequest.productId());

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .originalPrice(product.getPrice())
                    .discountPercentage(product.getDiscountPercentage())
                    .unitPrice(product.getEffectivePrice())
                    .quantity(itemRequest.quantity())
                    .build();

            orderItems.add(item);

            totalAmount = totalAmount.add(
                    item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .paymentDeadline(LocalDateTime.now().plus(OrderConstants.PAYMENT_WINDOW))
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        return orderRepository.save(order);
    }
}
