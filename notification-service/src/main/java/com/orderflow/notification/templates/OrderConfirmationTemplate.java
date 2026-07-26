package com.orderflow.notification.templates;

import com.orderflow.notification.dto.EmailData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class OrderConfirmationTemplate {

    public String render(EmailData message) {
        String itemsHtml = message.items().stream()
                .map(this::renderItemRow)
                .collect(Collectors.joining());

        EmailData.ShippingAddress address = message.shippingAddress();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                <style>
                  @media only screen and (max-width: 620px) {
                    .email-container { width: 100%% !important; }
                    .content-padding { padding: 20px !important; }
                    .header-padding { padding: 20px !important; }
                    .product-name { font-size: 13px !important; }
                    .product-price { font-size: 12px !important; }
                    .product-image { width: 44px !important; height: 44px !important; }
                    .total-row { font-size: 15px !important; }
                  }
                </style>
                </head>
                <body style="margin: 0; padding: 0; background-color: #f4f4f5; font-family: Arial, Helvetica, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 24px 12px;">
                        <tr>
                            <td align="center">
                                <table class="email-container" width="600" style="max-width: 600px; width: 100%%; background-color: #ffffff; border-radius: 8px; overflow: hidden;" cellpadding="0" cellspacing="0">
                                    <tr>
                                        <td class="header-padding" style="background-color: #111827; padding: 24px 32px;">
                                            <h1 style="color: #ffffff; font-size: 20px; margin: 0;">OrderFlow</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="content-padding" style="padding: 32px;">
                                            <h2 style="font-size: 18px; color: #111827; margin: 0 0 8px 0;">Order confirmed!</h2>
                                            <p style="color: #6b7280; font-size: 14px; margin: 0 0 24px 0;">
                                                Hi %s, your order #%d has been paid successfully.
                                            </p>

                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom: 24px;">
                                                %s
                                            </table>

                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom: 24px;">
                                                <tr>
                                                    <td class="total-row" style="text-align: right; font-size: 16px; font-weight: bold; color: #111827; padding-top: 8px; border-top: 2px solid #111827;">
                                                        Total: %.2f€
                                                    </td>
                                                </tr>
                                            </table>

                                            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f9fafb; border-radius: 6px;">
                                                <tr>
                                                    <td style="padding: 16px;">
                                                        <p style="margin: 0 0 4px 0; font-size: 13px; font-weight: bold; color: #111827;">Shipping address</p>
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
                                        <td class="content-padding" style="background-color: #f9fafb; padding: 20px 32px; text-align: center;">
                                            <p style="color: #9ca3af; font-size: 12px; margin: 0;">Thank you for shopping with OrderFlow.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
            """
            .formatted(
                message.userFirstName(),
                message.orderId(),
                itemsHtml,
                message.totalAmount(),
                address.street(),
                address.postalCode(), address.city(),
                address.country()
            );
    }

    private String renderItemRow(EmailData.OrderItemSummary item) {
        boolean hasDiscount = item.discountPercentage() > 0;
        BigDecimal lineTotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));

        String imageUrl = item.productImageUrl() != null
                ? item.productImageUrl()
                : "https://via.placeholder.com/60";

        String discountBadge = hasDiscount
                ? """
                  &nbsp;·&nbsp;
                  <span style="text-decoration: line-through;">%.2f€</span>
                  <span style="background-color: #fef2f2; color: #b91c1c; font-size: 11px; font-weight: bold; padding: 1px 5px; border-radius: 3px; margin-left: 4px;">-%d%%</span>
                  """.formatted(item.originalPrice(), item.discountPercentage())
                : "";

        return """
                <tr>
                    <td style="padding: 14px 0; border-bottom: 1px solid #eee; width: 56px;" valign="top">
                        <img class="product-image" src="%s" width="50" height="50" style="border-radius: 4px; object-fit: cover; display: block;" alt="%s">
                    </td>
                    <td class="product-name" style="padding: 14px 0 14px 12px; border-bottom: 1px solid #eee; font-size: 14px; color: #374151;" valign="top">
                        <div>%s</div>
                        <div style="color: #9ca3af; font-size: 12px; margin-top: 2px;">
                            Qty: %d%s
                        </div>
                    </td>
                    <td class="product-price" style="padding: 14px 0; border-bottom: 1px solid #eee; text-align: right; font-size: 13px; white-space: nowrap; color: #111827;" valign="top">
                        <div>%.2f€ <span style="color: #9ca3af; font-weight: normal;">/unit</span></div>
                        <div style="font-weight: bold; margin-top: 2px;">%.2f€</div>
                    </td>
                </tr>
                """
                .formatted(
                    imageUrl, item.productName(),
                    item.productName(),
                    item.quantity(), discountBadge,
                    item.unitPrice(),
                    lineTotal
                );
    }
}
