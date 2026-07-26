package com.orderflow.order.dto.order;

import com.orderflow.order.dto.addresses.ShippingAddress;

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
    public record OrderItemSummary(
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal originalPrice,
            int discountPercentage,
            BigDecimal unitPrice
    ) {}
}