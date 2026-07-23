package com.orderflow.order.config;

import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor
public class OrderConstants {
    public static final Duration PAYMENT_WINDOW = Duration.ofMinutes(2);
}
