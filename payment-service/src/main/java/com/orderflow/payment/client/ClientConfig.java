package com.orderflow.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Value("${order-service.url}")
    private String orderServiceUrl;

    @Bean
    public RestClient orderServiceRestClient() {
        return RestClient.builder()
                .baseUrl(orderServiceUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }
}
