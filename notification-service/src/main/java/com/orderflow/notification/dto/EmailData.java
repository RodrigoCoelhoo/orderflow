package com.orderflow.notification.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmailData(
        Long orderId,
        String userEmail,
        String userFirstName,
        BigDecimal totalAmount,
        ShippingAddress shippingAddress,
        List<OrderItemSummary> items
) {
    public record ShippingAddress(
            String street,
            String city,
            String postalCode,
            String country
    ) {}

    public record OrderItemSummary(
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal originalPrice,
            int discountPercentage,
            BigDecimal unitPrice
    ) {}
}