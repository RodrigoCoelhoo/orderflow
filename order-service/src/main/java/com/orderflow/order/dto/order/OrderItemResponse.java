package com.orderflow.order.dto.order;

import com.orderflow.order.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal originalPrice,
        int discountPercentage,
        BigDecimal unitPrice,
        Integer quantity
) {
    public static OrderItemResponse toDTO(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getOriginalPrice(),
                orderItem.getDiscountPercentage(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity()
        );
    }
}