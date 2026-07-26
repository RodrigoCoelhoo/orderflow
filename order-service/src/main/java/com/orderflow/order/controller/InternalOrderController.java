package com.orderflow.order.controller;

import com.orderflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PostMapping("/{paymentIntentId}/payment-succeeded")
    public ResponseEntity<Void> handlePaymentSucceeded(@PathVariable String paymentIntentId) {
        orderService.handlePaymentSucceeded(paymentIntentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{paymentIntentId}/payment-failed")
    public ResponseEntity<Void> handlePaymentFailed(@PathVariable String paymentIntentId) {
        orderService.handlePaymentFailed(paymentIntentId);
        return ResponseEntity.noContent().build();
    }
}
