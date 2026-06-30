package com.orderflow.order.dto.inventory;

public record StockResponse(
        Long productId,
        Integer availableQuantity
) {}