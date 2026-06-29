package com.orderflow.order.service;

import com.orderflow.order.dto.CreateProductRequest;
import com.orderflow.order.dto.ProductResponse;
import com.orderflow.order.dto.UpdateProductRequest;
import com.orderflow.order.repository.ProductRepository;
import com.orderflow.order.utils.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(
            int page,
            int size,
            String name
    ) {
        return null;
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(
            Long id
    ) {
        return null;
    }

    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest data
    ) {
        return null;
    }

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            @Valid UpdateProductRequest data
    ) {
        return null;
    }

    @Transactional
    public void deleteProduct(
            Long id
    ) {

    }
}
