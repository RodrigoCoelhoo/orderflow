package com.orderflow.inventory.service;

import com.orderflow.inventory.dto.*;
import com.orderflow.inventory.exceptions.ResourceAlreadyExistsException;
import com.orderflow.inventory.exceptions.ResourceNotFound;
import com.orderflow.inventory.model.StockItem;
import com.orderflow.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<StockResponse> getStockBatch(
            List<Long> productIds
    ) {
        Map<Long, StockItem> stockMap = stockRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(StockItem::getProductId, s -> s));

        return productIds.stream()
                .map(id -> {
                    StockItem item = stockMap.get(id);
                    int available = item != null ? item.getAvailableQuantity() : 0;
                    return new StockResponse(id, available);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public StockAvailabilityResponse checkAvailability(
            StockAvailabilityRequest request
    ) {
        List<StockAvailabilityResponse.UnavailableItem> unavailable = new ArrayList<>();

        for (StockCheckItem checkItem : request.items()) {
            StockItem stockItem = stockRepository.findByProductId(checkItem.productId()).orElse(null);
            int available = stockItem != null ? stockItem.getAvailableQuantity() : 0;

            if (available < checkItem.quantity()) {
                unavailable.add(
                        new StockAvailabilityResponse.UnavailableItem(
                            checkItem.productId(),
                            checkItem.quantity(),
                            available
                        )
                );

                log.warn(
                    "Insufficient stock [productId={}, requested={}, available={}]",
                    checkItem.productId(), checkItem.quantity(), available
                );
            }
        }

        return new StockAvailabilityResponse(unavailable.isEmpty(), unavailable);
    }

    @Transactional
    public void createStock(
            Long productId,
            Integer initialQuantity
    ) {
        if (stockRepository.findByProductId(productId).isPresent()) {
            throw new ResourceAlreadyExistsException("Stock already exists for product " + productId);
        }

        StockItem stockItem = StockItem.builder()
                .productId(productId)
                .quantity(initialQuantity)
                .build();

        stockRepository.save(stockItem);

        log.info("Stock created [productId={}, quantity={}]", productId, initialQuantity);
    }

    @Transactional
    public void adjustStock(
            Long productId,
            Integer newQuantity
    ) {
        StockItem item = getStockItemById(productId);
        item.setQuantity(newQuantity);
        stockRepository.save(item);

        log.info("Stock adjusted [productId={}, newQuantity={}]", productId, newQuantity);
    }

    @Transactional
    public void deleteStock(
            Long productId
    ) {
        StockItem item = getStockItemById(productId);
        stockRepository.delete(item);

        log.info("Stock deleted [productId={}]", productId);
    }

    public StockItem getStockItemById(
            Long productId
    ) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFound("Stock not found for product " + productId));
    }
}