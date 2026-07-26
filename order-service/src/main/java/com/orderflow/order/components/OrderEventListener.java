package com.orderflow.order.components;

import com.orderflow.order.config.RabbitMQConfig;
import com.orderflow.order.dto.addresses.ShippingAddress;
import com.orderflow.order.dto.order.EmailData;
import com.orderflow.order.dto.order.OrderCompletedEvent;
import com.orderflow.order.dto.order.OrderCreatedEvent;
import com.orderflow.order.model.Order;
import com.orderflow.order.model.User;
import com.orderflow.order.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final AmqpTemplate rabbitTemplate;
    private final UserService userService;

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent received for order {}", event.order().getId());
        Order order = event.order();
        var payload = Map.of("orderId", order.getId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAIN_EXCHANGE,
                RabbitMQConfig.ORDER_EXPIRY_QUEUE,
                payload,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
    }

    @TransactionalEventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        Order order = event.order();
        User user = userService.getUserById(order.getUserId());

        var items = order.getItems().stream()
                .map(item -> new EmailData.OrderItemSummary(
                        item.getProductName(),
                        item.getProductImageUrl(),
                        item.getQuantity(),
                        item.getOriginalPrice(),
                        item.getDiscountPercentage(),
                        item.getUnitPrice()
                )).toList();

        var shippingAddress = new ShippingAddress(
                order.getShippingStreet(),
                order.getShippingCity(),
                order.getShippingPostalCode(),
                order.getShippingCountry()
        );

        var payload = new EmailData(
                order.getId(),
                user.getEmail(),
                user.getFirstName(),
                order.getTotalAmount(),
                shippingAddress,
                items
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAIN_EXCHANGE,
                RabbitMQConfig.ORDER_COMPLETED_QUEUE,
                payload,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
    }
}
