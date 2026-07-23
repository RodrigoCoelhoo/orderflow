package com.orderflow.order.client;

import com.orderflow.order.dto.inventory.InsufficientStockResponse;
import com.orderflow.order.dto.inventory.StockReserveRequest;
import com.orderflow.order.dto.inventory.StockResponse;
import com.orderflow.order.exceptions.InsufficientStockException;
import com.orderflow.order.exceptions.InventoryServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestClient inventoryRestClient;

    public List<StockResponse> getStockBatch(
            List<Long> productIds
    ) {
        return inventoryRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/stock/batch")
                        .queryParam("productIds", productIds)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<StockResponse>>() {});
    }

    public void createStock(
            Long productId,
            Integer initialQuantity
    ) {
        inventoryRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/stock/{productId}")
                        .queryParam("initialQuantity", initialQuantity)
                        .build(productId))
                .retrieve()
                .toBodilessEntity();
    }

    public void adjustStock(
            Long productId,
            Integer newQuantity
    ) {
        inventoryRestClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/stock/{productId}")
                        .queryParam("newQuantity", newQuantity)
                        .build(productId))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteStock(
            Long productId
    ) {
        inventoryRestClient.delete()
                .uri("/internal/stock/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }

    public void reserveStock(
            StockReserveRequest request
    ) {
        try {
            inventoryRestClient.post()
                    .uri("/internal/stock/reserve")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            InsufficientStockResponse body = e.getResponseBodyAs(InsufficientStockResponse.class);
            throw new InsufficientStockException(
                    e.getMessage(),
                    body != null ? body.unavailableItems() : List.of()
            );
        } catch (RestClientException e) {
            log.error("Inventory service unavailable during reserveStock", e);
            throw new InventoryServiceUnavailableException("Inventory service unavailable");
        }
    }

    public void releaseStock(
            StockReserveRequest request
    ) {
        try {
            inventoryRestClient.post()
                    .uri("/internal/stock/release")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to release stock — manual intervention may be needed [request={}]", request, e);
        }
    }

    public void confirmStock(
            StockReserveRequest request
    ) {
        try {
            inventoryRestClient.post()
                    .uri("/internal/stock/confirm")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to confirm stock — manual intervention may be needed [request={}]", request, e);
        }
    }
}