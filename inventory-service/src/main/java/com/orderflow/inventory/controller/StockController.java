package com.orderflow.inventory.controller;

import com.orderflow.inventory.dto.*;
import com.orderflow.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/batch")
    public ResponseEntity<List<StockResponse>> getStockBatch(
            @RequestParam List<Long> productIds
    ) {
        return ResponseEntity.ok(stockService.getStockBatch(productIds));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> createStock(
            @PathVariable Long productId,
            @RequestParam Integer initialQuantity
    ) {
        stockService.createStock(productId, initialQuantity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer newQuantity
    ) {
        stockService.adjustStock(productId, newQuantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteStock(
            @PathVariable Long productId
    ) {
        stockService.deleteStock(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(
            @Valid @RequestBody StockReserveRequest request
    ) {
        stockService.reserveStock(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(
            @Valid @RequestBody StockReserveRequest request
    ) {
        stockService.releaseStock(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmStock(
            @Valid @RequestBody StockReserveRequest request
    ) {
        stockService.confirmStock(request);
        return ResponseEntity.noContent().build();
    }
}