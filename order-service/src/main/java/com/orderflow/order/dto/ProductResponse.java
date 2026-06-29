package com.orderflow.order.dto;

import com.orderflow.order.model.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int discountPercentage,
        BigDecimal effectivePrice,
        String imageUrl,
        Integer stock
) {
    public static ProductResponse toDto(Product data) {
        return new ProductResponse(
                data.getId(),
                data.getName(),
                data.getDescription(),
                data.getPrice(),
                data.getDiscountPercentage(),
                data.getEffectivePrice(),
                data.getImageUrl(),
                data.getStock()
        );
    }
}
