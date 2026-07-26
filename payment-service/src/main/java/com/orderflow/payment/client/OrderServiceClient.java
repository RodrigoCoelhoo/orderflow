package com.orderflow.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {

    private final RestClient orderServiceRestClient;

    public void notifyPaymentSucceeded(String paymentIntentId) {
        orderServiceRestClient.post()
            .uri("/internal/orders/{id}/payment-succeeded", paymentIntentId)
            .retrieve()
            .toBodilessEntity();
    }

    public void notifyPaymentFailed(String paymentIntentId) {
        orderServiceRestClient.post()
            .uri("/internal/orders/{id}/payment-failed", paymentIntentId)
            .retrieve()
            .toBodilessEntity();
    }
}
