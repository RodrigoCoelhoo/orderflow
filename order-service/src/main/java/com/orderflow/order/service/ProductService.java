package com.orderflow.order.service;

import com.orderflow.order.client.InventoryClient;
import com.orderflow.order.dto.inventory.StockResponse;
import com.orderflow.order.dto.product.CreateProductRequest;
import com.orderflow.order.dto.product.ProductResponse;
import com.orderflow.order.dto.product.UpdateProductRequest;
import com.orderflow.order.exceptions.ResourceNotFound;
import com.orderflow.order.model.Product;
import com.orderflow.order.repository.ProductRepository;
import com.orderflow.order.utils.PagedResponse;
import com.orderflow.order.utils.cloudinary.CloudinaryService;
import com.orderflow.order.utils.cloudinary.CloudinaryUploadResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final InventoryClient inventoryClient;

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

        List<Long> productIds = products.getContent().stream().map(Product::getId).toList();
        Map<Long, Integer> stockMap = inventoryClient.getStockBatch(productIds).stream()
                .collect(Collectors.toMap(StockResponse::productId, StockResponse::availableQuantity));

        List<ProductResponse> content = products.getContent().stream()
                .map(p -> ProductResponse.toDto(p, stockMap.getOrDefault(p.getId(), 0)))
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
        List<StockResponse> stock = inventoryClient.getStockBatch(List.of(product.getId()));

        return ProductResponse.toDto(product, stock.getFirst().availableQuantity());
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
    ) throws IOException {
        log.info("Creating product '{}'", data.name());

        Product product = Product.builder()
                .name(data.name())
                .description(data.description())
                .price(data.price())
                .discountPercentage(data.discountPercentage())
                .discountExpiresAt(data.discountExpiresAt())
                .build();

        if (data.image() != null && !data.image().isEmpty()) {
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(data.image(), "products");
            product.setImageUrl(uploadResult.secureUrl());
            product.setImagePublicId(uploadResult.publicId());
        }

        Product saved = productRepository.save(product);

        inventoryClient.createStock(saved.getId(), data.initialStock());

        log.info("Product {} created successfully with id={}", saved.getName(), saved.getId());

        return ProductResponse.toDto(saved, data.initialStock());
    }

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            @Valid UpdateProductRequest data
    ) throws IOException {
        log.info("Updating product with id={}", id);

        Product product = getProductById(id);

        if(data.name() != null) product.setName(data.name());
        if(data.description() != null) product.setDescription(data.description());
        if(data.price() != null) product.setPrice(data.price());
        if(data.discountPercentage() != null) product.setDiscountPercentage(data.discountPercentage());
        if(data.discountExpiresAt() != null) product.setDiscountExpiresAt(data.discountExpiresAt());
        if (data.image() != null && !data.image().isEmpty()) {

            if (product.getImagePublicId() != null) {
                cloudinaryService.deleteImage(product.getImagePublicId());
            }

            CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(data.image(), "products");
            product.setImageUrl(uploadResult.secureUrl());
            product.setImagePublicId(uploadResult.publicId());
        }

        Product saved = productRepository.save(product);

        Integer updatedStock = data.stock() != null ? data.stock() : null;

        if (updatedStock != null) {
            inventoryClient.adjustStock(id, updatedStock);
        }

        List<StockResponse> stock = inventoryClient.getStockBatch(List.of(saved.getId()));

        log.info("Product with id={} updated successfully", id);

        return ProductResponse.toDto(saved, stock.getFirst().availableQuantity());
    }

    @Transactional
    public void deleteProduct(
            Long id
    ) throws IOException {
        log.info("Deleting product with id={}", id);

        Product product = getProductById(id);

        if (product.getImagePublicId() != null) {
            cloudinaryService.deleteImage(product.getImagePublicId());
        }

        productRepository.delete(product);

        inventoryClient.deleteStock(id);

        log.info("Product with id={} deleted successfully", id);
    }
}
