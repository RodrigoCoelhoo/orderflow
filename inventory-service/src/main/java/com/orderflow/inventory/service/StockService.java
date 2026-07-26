package com.orderflow.inventory.service;

import com.orderflow.inventory.dto.*;
import com.orderflow.inventory.dto.StockAvailabilityResponse.UnavailableItem;
import com.orderflow.inventory.exceptions.InsufficientStockException;
import com.orderflow.inventory.exceptions.ResourceAlreadyExistsException;
import com.orderflow.inventory.exceptions.ResourceNotFound;
import com.orderflow.inventory.exceptions.BadRequestException;
import com.orderflow.inventory.model.StockItem;
import com.orderflow.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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

        if(newQuantity < item.getReservedQuantity()) {
            throw new BadRequestException("Stock quantity cannot be lower than the reserved quantity.");
        }

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

    @Transactional
    public void reserveStock(StockReserveRequest request) {
        Map<Long, StockItem> stock = loadStockItemsForUpdate(
                request.items().stream()
                        .map(StockReserveItem::productId)
                        .toList()
        );

        List<UnavailableItem> unavailable = new ArrayList<>();

        for (StockReserveItem item : request.items()) {
            StockItem stockItem = stock.get(item.productId());

            int available = stockItem.getAvailableQuantity();

            if (available < item.quantity()) {
                unavailable.add(new UnavailableItem(
                        item.productId(),
                        item.quantity(),
                        available
                ));
                continue;
            }

            stockItem.setReservedQuantity(
                    stockItem.getReservedQuantity() + item.quantity()
            );
        }

        if (!unavailable.isEmpty()) {
            throw new InsufficientStockException("Insufficient stock", unavailable);
        }

        stockRepository.saveAll(stock.values());
        log.info("Stock reserved for {} items", request.items().size());
    }

    @Transactional
    public void releaseStock(StockReserveRequest request) {
        Map<Long, StockItem> stock = loadStockItemsForUpdate(
                request.items().stream()
                        .map(StockReserveItem::productId)
                        .toList()
        );

        for (StockReserveItem item : request.items()) {
            StockItem stockItem = stock.get(item.productId());

            if (stockItem.getReservedQuantity() < item.quantity()) {
                throw new BadRequestException(
                        "Cannot release more stock than is reserved for product " + item.productId()
                );
            }

            stockItem.setReservedQuantity(
                    stockItem.getReservedQuantity() - item.quantity()
            );
        }

        stockRepository.saveAll(stock.values());
        log.info("Stock released for {} items", request.items().size());
    }

    @Transactional
    public void confirmStock(StockReserveRequest request) {
        Map<Long, StockItem> stock = loadStockItemsForUpdate(
                request.items().stream()
                        .map(StockReserveItem::productId)
                        .toList()
        );

        for (StockReserveItem item : request.items()) {
            StockItem stockItem = stock.get(item.productId());

            if (stockItem.getReservedQuantity() < item.quantity()) {
                throw new BadRequestException(
                        "Cannot confirm more stock than is reserved for product " + item.productId()
                );
            }

            stockItem.setReservedQuantity(stockItem.getReservedQuantity() - item.quantity());
            stockItem.setQuantity(stockItem.getQuantity() - item.quantity());
        }

        stockRepository.saveAll(stock.values());
        log.info("Stock confirmed for {} items", request.items().size());
    }

    private Map<Long, StockItem> loadStockItemsForUpdate(Collection<Long> productIds) {
        if(new HashSet<>(productIds).size() != productIds.size())
            throw new BadRequestException("Duplicate productId in request.");

        List<Long> sortedIds = productIds.stream()
                .sorted()
                .toList();

        Map<Long, StockItem> stock = stockRepository
                .findByProductIdInForUpdate(sortedIds)
                .stream()
                .collect(Collectors.toMap(
                        StockItem::getProductId,
                        Function.identity()
                ));

        for (Long productId : sortedIds) {
            if (!stock.containsKey(productId)) {
                throw new ResourceNotFound("Stock not found for product " + productId);
            }
        }

        return stock;
    }
}