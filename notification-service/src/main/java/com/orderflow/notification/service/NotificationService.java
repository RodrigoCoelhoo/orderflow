package com.orderflow.notification.service;

import com.orderflow.notification.dto.EmailData;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String fromAddress;

    @Value("${resend.to}")
    private String toAddress;

    public void sendOrderConfirmation(EmailData message) {
        Resend resend = new Resend(apiKey);

        String itemsHtml = message.items().stream()
                .map(this::renderItemRow)
                .collect(Collectors.joining());

        EmailData.ShippingAddress address = message.shippingAddress();

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="margin: 0; padding: 0; background-color: #f4f4f5; font-family: Arial, Helvetica, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden;">
                                    <tr>
                                        <td style="background-color: #111827; padding: 24px 32px;">
                                            <h1 style="color: #ffffff; font-size: 20px; margin: 0;">OrderFlow</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 32px;">
                                            <h2 style="font-size: 18px; color: #111827; margin: 0 0 8px 0;">Order confirmed!</h2>
                                            <p style="color: #6b7280; font-size: 14px; margin: 0 0 24px 0;">
                                                Hello %s, your order #%d has been succesfully paid.
                                            </p>

                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom: 24px;">
                                                %s
                                            </table>

                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom: 24px;">
                                                <tr>
                                                    <td style="text-align: right; font-size: 16px; font-weight: bold; color: #111827; padding-top: 8px; border-top: 2px solid #111827;">
                                                        Total: %.2f€
                                                    </td>
                                                </tr>
                                            </table>

                                            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f9fafb; border-radius: 6px;">
                                                <tr>
                                                    <td style="padding: 16px;">
                                                        <p style="margin: 0 0 4px 0; font-size: 13px; font-weight: bold; color: #111827;">Address</p>
                                                        <p style="margin: 0; font-size: 13px; color: #6b7280; line-height: 1.5;">
                                                            %s<br>
                                                            %s, %s<br>
                                                            %s
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color: #f9fafb; padding: 20px 32px; text-align: center;">
                                            <p style="color: #9ca3af; font-size: 12px; margin: 0;">Thank you for buying on OrderFlow.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                    message.userFirstName(),
                    message.orderId(),
                    itemsHtml,
                    message.totalAmount(),
                    address.street(),
                    address.postalCode(), address.city(),
                    address.country()
            );

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                //.to(message.userEmail())
                .to(toAddress) // Render free plan only allows to send to own email
                .subject("OrderFlow — Confirmação da Encomenda #" + message.orderId())
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

    private String renderItemRow(EmailData.OrderItemSummary item) {
        boolean hasDiscount = item.discountPercentage() > 0;

        String priceHtml = hasDiscount
                ? """
                  <span style="text-decoration: line-through; color: #9ca3af; font-size: 12px;">%.2f€</span>
                  <span style="color: #dc2626; font-weight: bold;"> %.2f€</span>
                  """.formatted(item.originalPrice(), item.unitPrice())
                : "<span>%.2f€</span>".formatted(item.unitPrice());

        String imageUrl = item.productImageUrl() != null
                ? item.productImageUrl()
                : "https://via.placeholder.com/60";

        return """
                <tr>
                    <td style="padding: 12px 0; border-bottom: 1px solid #eee; width: 60px;">
                        <img src="%s" width="50" height="50" style="border-radius: 4px; object-fit: cover;" alt="%s">
                    </td>
                    <td style="padding: 12px 0 12px 12px; border-bottom: 1px solid #eee; font-size: 14px; color: #374151;">
                        %s <span style="color: #9ca3af;">x%d</span>
                    </td>
                    <td style="padding: 12px 0; border-bottom: 1px solid #eee; text-align: right; font-size: 13px;">
                        %s
                    </td>
                </tr>
                """.formatted(imageUrl, item.productName(), item.productName(), item.quantity(), priceHtml);
    }
}