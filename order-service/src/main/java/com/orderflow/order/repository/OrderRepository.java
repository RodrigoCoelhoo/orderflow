package com.orderflow.order.repository;

import com.orderflow.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);
}
