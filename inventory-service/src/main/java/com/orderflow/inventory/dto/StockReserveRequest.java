package com.orderflow.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StockReserveRequest(
        @NotEmpty @Valid List<StockReserveItem> items
) {}