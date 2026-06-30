package com.orderflow.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InventoryClientConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Bean
    public RestClient inventoryRestClient() {
        return RestClient.builder()
                .baseUrl("http://inventory-service:8084")
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }
}