package com.orderflow.inventory.dto;

import java.util.List;

public record StockAvailabilityResponse(
        boolean allAvailable,
        List<UnavailableItem> unavailableItems
) {
    public record UnavailableItem(
            Long productId,
            Integer requestedQuantity,
            Integer availableQuantity
    ) {}
}