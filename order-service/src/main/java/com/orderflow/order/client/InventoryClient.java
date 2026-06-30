package com.orderflow.order.client;

import com.orderflow.order.dto.inventory.StockAvailabilityRequest;
import com.orderflow.order.dto.inventory.StockAvailabilityResponse;
import com.orderflow.order.dto.inventory.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestClient inventoryRestClient;

    public List<StockResponse> getStockBatch(List<Long> productIds) {
        return inventoryRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/stock/batch")
                        .queryParam("productIds", productIds)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<StockResponse>>() {});
    }

    public StockAvailabilityResponse checkAvailability(StockAvailabilityRequest request) {
        return inventoryRestClient.post()
                .uri("/internal/stock/check-availability")
                .body(request)
                .retrieve()
                .body(StockAvailabilityResponse.class);
    }

    public void createStock(Long productId, Integer initialQuantity) {
        inventoryRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/stock/{productId}")
                        .queryParam("initialQuantity", initialQuantity)
                        .build(productId))
                .retrieve()
                .toBodilessEntity();
    }

    public void adjustStock(Long productId, Integer newQuantity) {
        inventoryRestClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/stock/{productId}")
                        .queryParam("newQuantity", newQuantity)
                        .build(productId))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteStock(Long productId) {
        inventoryRestClient.delete()
                .uri("/internal/stock/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }
}