package com.orderflow.order.dto;

import com.orderflow.order.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int discountPercentage,
        LocalDateTime discountExpiresAt,
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
                data.getDiscountExpiresAt(),
                data.getEffectivePrice(),
                data.getImageUrl(),
                data.getStock()
        );
    }
}
