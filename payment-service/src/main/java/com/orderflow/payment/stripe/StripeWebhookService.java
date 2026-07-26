package com.orderflow.payment.stripe;

import com.orderflow.payment.client.OrderServiceClient;
import com.orderflow.payment.exceptions.InvalidStripeWebhookException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final OrderServiceClient orderServiceClient;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public void handleWebhook(String payload, String signature) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new InvalidStripeWebhookException();
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            default -> log.info("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handlePaymentSucceeded(Event event) {
        PaymentIntent intent = getPaymentIntent(event);
        orderServiceClient.notifyPaymentSucceeded(intent.getId());
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent intent = getPaymentIntent(event);
        orderServiceClient.notifyPaymentFailed(intent.getId());
    }

    private PaymentIntent getPaymentIntent(Event event) {
        return (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow();
    }
}