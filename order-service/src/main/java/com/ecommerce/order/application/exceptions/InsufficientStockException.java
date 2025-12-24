package com.ecommerce.order.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when there's insufficient stock for the requested quantity.
 */
public class InsufficientStockException extends RuntimeException {

    private final UUID productId;
    private final int requestedQuantity;

    public InsufficientStockException(UUID productId, int requestedQuantity) {
        super("Insufficient stock for product " + productId + ". Requested: " + requestedQuantity);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
