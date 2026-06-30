package com.orderflow.inventory.dto;

public record StockResponse(
        Long productId,
        Integer availableQuantity
) {}