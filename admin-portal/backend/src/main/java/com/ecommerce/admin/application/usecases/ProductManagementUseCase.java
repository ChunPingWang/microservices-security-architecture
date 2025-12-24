package com.ecommerce.admin.application.usecases;

import com.ecommerce.admin.application.dto.ProductSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use case for product management operations.
 * Note: In a real implementation, this would call the product-service via Feign client.
 */
@Service
public class ProductManagementUseCase {

    // Mock data store for demo purposes
    private final Map<UUID, ProductSummary> products = new ConcurrentHashMap<>();

    public ProductManagementUseCase() {
        // Initialize with some mock data
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        products.put(id1, new ProductSummary(id1, "SKU001", "iPhone 15", new BigDecimal("35900"), 100, true));
        products.put(id2, new ProductSummary(id2, "SKU002", "MacBook Pro", new BigDecimal("59900"), 50, true));
    }

    /**
     * Lists all products with optional filtering.
     */
    public List<ProductSummary> listProducts(UUID adminId) {
        return new ArrayList<>(products.values());
    }

    /**
     * Updates product stock.
     */
    public boolean updateStock(UUID adminId, UUID productId, Integer quantity) {
        ProductSummary existing = products.get(productId);
        if (existing == null) {
            return false;
        }

        ProductSummary updated = new ProductSummary(
                existing.productId(),
                existing.sku(),
                existing.name(),
                existing.price(),
                quantity,
                existing.active()
        );
        products.put(productId, updated);
        return true;
    }

    /**
     * Toggles product active status.
     */
    public boolean toggleProductStatus(UUID adminId, UUID productId, Boolean active) {
        ProductSummary existing = products.get(productId);
        if (existing == null) {
            return false;
        }

        ProductSummary updated = new ProductSummary(
                existing.productId(),
                existing.sku(),
                existing.name(),
                existing.price(),
                existing.stock(),
                active
        );
        products.put(productId, updated);
        return true;
    }
}
