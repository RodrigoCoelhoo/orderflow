package com.orderflow.order.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockCheckItem(
        @NotNull Long productId,
        @Min(1) Integer quantity
) {}