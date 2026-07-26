package com.orderflow.order.service;

import com.orderflow.order.exceptions.PaymentServiceUnavailableException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class StripeService {

    @Value("${stripe.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public String createPaymentIntent(
            BigDecimal amount
    ) {
        try {
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        }
        catch (StripeException e) {
            log.error("Failed to create PaymentIntent", e);
            throw new PaymentServiceUnavailableException("Failed to create PaymentIntent");
        }
    }

    public String extractPaymentIntentId(String clientSecret) {
        return clientSecret.split("_secret_")[0];
    }

    public void cancelPaymentIntent(String paymentIntentId) {
        if (paymentIntentId == null) return;

        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent.cancel();
        }
        catch (StripeException e) {
            log.error("Failed to cancel PaymentIntent [id={}]", paymentIntentId, e);
        }
    }

    public void refundPaymentIntent(String paymentIntentId) {
        if (paymentIntentId == null) return;

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();

            Refund.create(params);
            log.info("Refunded PaymentIntent [id={}]", paymentIntentId);
        }
        catch (StripeException e) {
            log.error("Failed to refund PaymentIntent [id={}]", paymentIntentId, e);
        }
    }
}