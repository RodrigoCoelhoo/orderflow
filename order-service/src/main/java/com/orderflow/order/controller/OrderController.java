package com.orderflow.order.controller;

import com.orderflow.order.dto.order.CreateOrderRequest;
import com.orderflow.order.dto.order.OrderResponse;
import com.orderflow.order.dto.auth.UserProfile;
import com.orderflow.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal UserProfile userProfile
    ) {
        OrderResponse response = orderService.createOrder(request, userProfile.id(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserProfile userProfile
    ) {
        return ResponseEntity.ok(orderService.getOrderById(id, userProfile.id()));
    }
}