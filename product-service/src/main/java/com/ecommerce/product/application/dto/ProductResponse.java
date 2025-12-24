package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.entities.Inventory;
import com.ecommerce.product.domain.entities.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for product data.
 */
public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        UUID categoryId,
        boolean active,
        List<String> imageUrls,
        StockInfo stockInfo,
        Instant createdAt
) {
    public record StockInfo(
            int available,
            boolean inStock,
            boolean lowStock
    ) {
    }

    public static ProductResponse from(Product product, Inventory inventory) {
        StockInfo stockInfo = inventory != null
                ? new StockInfo(
                inventory.getAvailableQuantity(),
                !inventory.isOutOfStock(),
                inventory.isLowStock()
        )
                : new StockInfo(0, false, true);

        return new ProductResponse(
                product.getId(),
                product.getSku().getValue(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency().getCurrencyCode(),
                product.getCategoryId(),
                product.isActive(),
                product.getImageUrls(),
                stockInfo,
                product.getCreatedAt()
        );
    }

    public static ProductResponse fromProductOnly(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku().getValue(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency().getCurrencyCode(),
                product.getCategoryId(),
                product.isActive(),
                product.getImageUrls(),
                null,
                product.getCreatedAt()
        );
    }
}
