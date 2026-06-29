package com.orderflow.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String description;
    private String imageUrl;
    private String imagePublicId;

    // Note: single currency assumed (EUR). Multi-currency support out of scope.
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(nullable = false) private int discountPercentage;
    private LocalDateTime discountExpiresAt;

    @Column(nullable = false) private Integer stock;

    @CreatedDate private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;

    public BigDecimal getEffectivePrice() {
        if (discountPercentage == 0.0) return price;
        if (discountExpiresAt != null && discountExpiresAt.isBefore(LocalDateTime.now())) return price;
        BigDecimal discount = BigDecimal.valueOf(discountPercentage / 100.0);
        return price.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }
}
