package com.orderflow.order.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateProductRequest(
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Invalid price format")
        BigDecimal price,

        @Min(value = 0, message = "Discount must be at least 0")
        @Max(value = 100, message = "Discount cannot exceed 100")
        Integer discountPercentage,

        LocalDateTime discountExpiresAt,

        String imageUrl,

        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock
) {}