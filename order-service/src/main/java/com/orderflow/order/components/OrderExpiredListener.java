package com.orderflow.order.components;

import com.orderflow.order.config.RabbitMQConfig;
import com.orderflow.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiredListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_EXPIRED_QUEUE, ackMode = "MANUAL")
    public void handleOrderExpired(
            Map<String, Object> payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        try {
            Long orderId = ((Number) payload.get("orderId")).longValue();
            orderService.expireOrder(orderId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process order expiry", e);
            channel.basicNack(tag, false, true); // requeue
        }
    }
}