package com.orderflow.order.dto.product;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateProductRequest(
        @NotBlank(message = "Name cannot be blank")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Description cannot be blank")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Invalid price format")
        BigDecimal price,

        @NotNull(message = "Discount percentage cannot be null")
        @Min(value = 0, message = "Discount must be at least 0")
        @Max(value = 100, message = "Discount cannot exceed 100")
        Integer discountPercentage,

        LocalDateTime discountExpiresAt,

        MultipartFile image,

        @NotNull(message = "Stock cannot be null")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer initialStock
) {}
