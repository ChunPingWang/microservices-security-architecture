package com.ecommerce.order.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a cart item is not found.
 */
public class CartItemNotFoundException extends RuntimeException {

    private final UUID productId;

    public CartItemNotFoundException(UUID productId) {
        super("Cart item not found for product: " + productId);
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}
