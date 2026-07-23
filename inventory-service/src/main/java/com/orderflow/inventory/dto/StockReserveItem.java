package com.orderflow.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockReserveItem(
        @NotNull Long productId,
        @Min(1) Integer quantity
) {}