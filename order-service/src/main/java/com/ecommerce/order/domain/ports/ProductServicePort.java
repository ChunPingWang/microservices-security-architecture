package com.ecommerce.order.domain.ports;

import com.ecommerce.shared.domain.value_objects.Money;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for communicating with Product Service.
 */
public interface ProductServicePort {

    /**
     * Gets product information for cart display.
     */
    Optional<ProductInfo> getProductInfo(UUID productId);

    /**
     * Checks if the requested quantity is available in stock.
     */
    boolean isStockAvailable(UUID productId, int quantity);

    /**
     * Product information needed for cart operations.
     */
    record ProductInfo(
            UUID id,
            String name,
            String sku,
            Money price,
            int availableStock,
            boolean active
    ) {}
}
