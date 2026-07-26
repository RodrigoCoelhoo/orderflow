package com.orderflow.notification.components;

import com.orderflow.notification.config.RabbitMQConfig;
import com.orderflow.notification.dto.EmailData;
import com.orderflow.notification.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCompletedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
    public void handleOrderCompleted(
            EmailData message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        try {
            emailService.sendOrderConfirmation(message);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process order.completed [orderId={}]", message.orderId(), e);
            channel.basicNack(tag, false, true);
        }
    }
}