package com.ecommerce.product.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

/**
 * Domain event emitted when product stock falls below threshold.
 */
public final class LowStockDetected extends DomainEvent {

    private final int currentStock;
    private final int threshold;

    public LowStockDetected(String productId, int currentStock, int threshold) {
        super(productId, "Product");
        this.currentStock = currentStock;
        this.threshold = threshold;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getThreshold() {
        return threshold;
    }
}
