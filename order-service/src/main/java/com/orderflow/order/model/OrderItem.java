package com.orderflow.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;     // price before discount

    @Column(nullable = false)
    private int discountPercentage;       // discount applied at purchase time, 0 if none

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;         // effective price actually paid

    @Column(nullable = false)
    private Integer quantity;
}