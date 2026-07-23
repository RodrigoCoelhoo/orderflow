package com.orderflow.inventory.repository;

import com.orderflow.inventory.model.StockItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductId(Long productId);
    List<StockItem> findByProductIdIn(List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM StockItem s
        WHERE s.productId IN :productIds
        ORDER BY s.productId
    """)
    List<StockItem> findByProductIdInForUpdate(@Param("productIds") Collection<Long> productIds);
}