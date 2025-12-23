package com.ecommerce.product.application.usecases;

import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.ProductSearchResult;
import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.InventoryRepository;
import com.ecommerce.product.domain.ports.ProductSearchPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for searching products.
 */
@Service
public class SearchProductsUseCase {

    private final ProductSearchPort productSearchPort;
    private final InventoryRepository inventoryRepository;

    public SearchProductsUseCase(
            ProductSearchPort productSearchPort,
            InventoryRepository inventoryRepository
    ) {
        this.productSearchPort = productSearchPort;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Searches products by keyword.
     */
    @Transactional(readOnly = true)
    public ProductSearchResult execute(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return ProductSearchResult.of(List.of(), 0, page, size);
        }

        List<Product> products = productSearchPort.search(keyword.trim(), page, size);

        List<ProductResponse> responses = products.stream()
                .map(this::toResponseWithStock)
                .toList();

        // Note: For accurate pagination, search port should return total count
        return ProductSearchResult.of(responses, responses.size(), page, size);
    }

    /**
     * Searches products by keyword within a category.
     */
    @Transactional(readOnly = true)
    public ProductSearchResult executeInCategory(String keyword, UUID categoryId, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return ProductSearchResult.of(List.of(), 0, page, size);
        }

        List<Product> products = productSearchPort.searchInCategory(
                keyword.trim(), categoryId, page, size
        );

        List<ProductResponse> responses = products.stream()
                .map(this::toResponseWithStock)
                .toList();

        return ProductSearchResult.of(responses, responses.size(), page, size);
    }

    /**
     * Gets search suggestions.
     */
    public List<String> getSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.length() < 2) {
            return List.of();
        }
        return productSearchPort.suggest(prefix, limit);
    }

    private ProductResponse toResponseWithStock(Product product) {
        return inventoryRepository.findByProductId(product.getId())
                .map(inventory -> ProductResponse.from(product, inventory))
                .orElse(ProductResponse.fromProductOnly(product));
    }
}
