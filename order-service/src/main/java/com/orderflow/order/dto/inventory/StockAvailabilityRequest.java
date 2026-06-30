package com.orderflow.order.dto.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StockAvailabilityRequest(
        @NotEmpty @Valid List<StockCheckItem> items
) {}