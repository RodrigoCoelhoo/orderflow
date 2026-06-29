package com.orderflow.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
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

    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Builder.Default @Column(nullable = false) private double discountPercentage = 0.0;
    @Builder.Default private LocalDateTime discountExpiresAt = null;

    @Builder.Default @Column(nullable = false) private Integer stock = 0;

    @CreatedDate private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
}
