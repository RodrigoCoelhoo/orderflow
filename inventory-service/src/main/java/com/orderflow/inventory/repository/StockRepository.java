package com.orderflow.inventory.repository;

import com.orderflow.inventory.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductId(Long productId);
    List<StockItem> findByProductIdIn(List<Long> productIds);
}