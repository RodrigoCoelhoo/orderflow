package com.orderflow.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queues store messages until a consumer reads them.
    public static final String ORDER_EXPIRY_QUEUE = "order.expiry";
    public static final String ORDER_EXPIRED_QUEUE = "order.expired";
    public static final String ORDER_COMPLETED_QUEUE = "order.completed";

    // Exchanges receive messages and route them to queues using routing keys.
    public static final String MAIN_EXCHANGE = "order.exchange";
    public static final String DLX_EXCHANGE = "order.dlx";

    // Main exchange used by the application to publish events.
    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange(MAIN_EXCHANGE);
    }

    // Dead Letter Exchange used by RabbitMQ to republish expired messages.
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // Routes messages published with the routing key "order.expiry"
    // to the order.expiry queue.
    @Bean
    public Binding orderExpiryBinding() {
        return BindingBuilder.bind(orderExpiryQueue())
                .to(mainExchange())
                .with(ORDER_EXPIRY_QUEUE);
    }

    // Holds newly created orders for 15 minutes.
    // When a message expires, RabbitMQ republishes it to the Dead Letter Exchange
    // using the routing key "order.expired".
    @Bean
    public Queue orderExpiryQueue() {
        return QueueBuilder.durable(ORDER_EXPIRY_QUEUE)
                .withArgument("x-message-ttl", OrderConstants.PAYMENT_WINDOW.toMillis())
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_EXPIRED_QUEUE)
                .build();
    }

    // Routes dead-lettered messages with the routing key "order.expired"
    // to the order.expired queue.
    @Bean
    public Binding orderExpiredBinding() {
        return BindingBuilder.bind(orderExpiredQueue())
                .to(dlxExchange())
                .with(ORDER_EXPIRED_QUEUE);
    }

    // Receives expired orders from the Dead Letter Exchange.
    // Consumed by the expiry worker.
    @Bean
    public Queue orderExpiredQueue() {
        return QueueBuilder.durable(ORDER_EXPIRED_QUEUE).build();
    }

    // Routes messages published with the routing key "order.completed"
    // to the order.completed queue.
    @Bean
    public Binding orderCompletedBinding() {
        return BindingBuilder.bind(orderCompletedQueue())
                .to(mainExchange())
                .with(ORDER_COMPLETED_QUEUE);
    }

    // Receives completed order events.
    // Intended to be consumed by services such as the notification service.
    @Bean
    public Queue orderCompletedQueue() {
        return QueueBuilder.durable(ORDER_COMPLETED_QUEUE).build();
    }

    // Serializes/deserializes RabbitMQ messages as JSON.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate used to publish messages to RabbitMQ.
    /*@Bean
    public AmqpTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter converter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }*/
}