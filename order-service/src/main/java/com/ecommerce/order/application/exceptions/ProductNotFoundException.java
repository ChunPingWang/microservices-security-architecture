package com.ecommerce.order.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends RuntimeException {

    private final UUID productId;

    public ProductNotFoundException(UUID productId) {
        super("Product not found: " + productId);
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}
