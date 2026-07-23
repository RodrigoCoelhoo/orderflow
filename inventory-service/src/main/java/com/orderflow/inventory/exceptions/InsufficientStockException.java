package com.orderflow.inventory.exceptions;

import com.orderflow.inventory.dto.StockAvailabilityResponse.UnavailableItem;
import lombok.Getter;

import java.util.List;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final List<UnavailableItem> unavailableItems;

    public InsufficientStockException(String message, List<UnavailableItem> unavailableItems) {
        super(message);
        this.unavailableItems = unavailableItems;
    }
}
