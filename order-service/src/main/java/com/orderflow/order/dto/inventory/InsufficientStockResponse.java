package com.orderflow.order.dto.inventory;

import com.orderflow.order.dto.inventory.StockAvailabilityResponse.UnavailableItem;

import java.util.List;

public record InsufficientStockResponse(
        String timestamp,
        int status,
        String error,
        String message,
        List<UnavailableItem> unavailableItems
) {}
