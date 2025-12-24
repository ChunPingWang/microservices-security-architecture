package com.ecommerce.product.application.usecases;

import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.ProductSearchResult;
import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.InventoryRepository;
import com.ecommerce.product.domain.ports.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for browsing products by category.
 */
@Service
public class BrowseProductsUseCase {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public BrowseProductsUseCase(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Gets all active products with pagination.
     */
    @Transactional(readOnly = true)
    public ProductSearchResult execute(int page, int size) {
        List<Product> products = productRepository.findAllActive(page, size);
        long totalElements = productRepository.countActive();

        List<ProductResponse> responses = products.stream()
                .map(this::toResponseWithStock)
                .toList();

        return ProductSearchResult.of(responses, totalElements, page, size);
    }

    /**
     * Gets products in a specific category.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> executeByCategory(UUID categoryId) {
        List<Product> products = productRepository.findByCategory(categoryId);

        return products.stream()
                .map(this::toResponseWithStock)
                .toList();
    }

    private ProductResponse toResponseWithStock(Product product) {
        return inventoryRepository.findByProductId(product.getId())
                .map(inventory -> ProductResponse.from(product, inventory))
                .orElse(ProductResponse.fromProductOnly(product));
    }
}
