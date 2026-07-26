package com.orderflow.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Value("${inventory.url}")
    private String inventoryUrl;

    @Bean
    public RestClient inventoryRestClient() {
        return RestClient.builder()
                .baseUrl(inventoryUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }
}