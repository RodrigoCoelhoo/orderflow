package com.orderflow.notification.service;

import com.orderflow.notification.dto.EmailData;
import com.orderflow.notification.templates.OrderConfirmationTemplate;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final OrderConfirmationTemplate emailTemplate;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String fromAddress;

    @Value("${resend.to}")
    private String toAddress;

    public void sendOrderConfirmation(EmailData message) {
        Resend resend = new Resend(apiKey);

        String html = emailTemplate.render(message);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                //.to(message.userEmail())
                .to(toAddress) // Render free plan only allows to send to own email
                .subject("OrderFlow - Order Confirmation #" + message.orderId())
                .html(html)
                .build();

        try {
            resend.emails().send(params);
            log.info("Order confirmation email sent [orderId={}, to={}]", message.orderId(), message.userEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email [orderId={}]", message.orderId(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}