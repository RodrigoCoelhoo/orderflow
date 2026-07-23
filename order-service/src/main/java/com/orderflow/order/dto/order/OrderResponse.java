package com.orderflow.order.dto.order;

import com.orderflow.order.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        BigDecimal totalAmount,
        String clientSecret,
        LocalDateTime paymentDeadline,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderResponse toDto(Order order, String clientSecret) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::toDTO)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                clientSecret,
                order.getPaymentDeadline(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}