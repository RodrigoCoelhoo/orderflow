package com.orderflow.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SagaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SagaServiceApplication.class, args);
    }
}
