package com.orderflow.order.service;

import com.orderflow.order.dto.CreateProductRequest;
import com.orderflow.order.dto.ProductResponse;
import com.orderflow.order.dto.UpdateProductRequest;
import com.orderflow.order.exceptions.ResourceNotFound;
import com.orderflow.order.model.Product;
import com.orderflow.order.repository.ProductRepository;
import com.orderflow.order.utils.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = (name != null && !name.isBlank())
                ? productRepository.findByNameContainingIgnoreCase(name, pageable)
                : productRepository.findAll(pageable);

        List<ProductResponse> content = products.getContent().stream()
                .map(ProductResponse::toDto)
                .toList();

        return new PagedResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages()
        );
    }

    public ProductResponse getProduct(
            Long id
    ) {
        Product product = getProductById(id);
        return ProductResponse.toDto(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(
            Long id
    ) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product with ID '%d' not found".formatted(id)));
    }

    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest data
    ) {
        Product product = Product.builder()
                .name(data.name())
                .description(data.description())
                .price(data.price())
                .discountPercentage(data.discountPercentage())
                .discountExpiresAt(data.discountExpiresAt())
                .imageUrl(data.imageUrl())
                .stock(data.stock())
                .build();

        Product saved = productRepository.save(product);
        return ProductResponse.toDto(saved);
    }

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            @Valid UpdateProductRequest data
    ) {
        Product product = getProductById(id);

        if(data.name() != null) product.setName(data.name());
        if(data.description() != null) product.setDescription(data.description());
        if(data.price() != null) product.setPrice(data.price());
        if(data.discountPercentage() != null) product.setDiscountPercentage(data.discountPercentage());
        if(data.discountExpiresAt() != null) product.setDiscountExpiresAt(data.discountExpiresAt());
        if(data.imageUrl() != null) product.setImageUrl(data.imageUrl());
        if(data.stock() != null) product.setStock(data.stock());

        Product saved = productRepository.save(product);
        return ProductResponse.toDto(saved);
    }

    @Transactional
    public void deleteProduct(
            Long id
    ) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
