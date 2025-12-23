package com.ecommerce.product.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

/**
 * Domain event emitted when product stock is completely depleted.
 */
public final class StockDepleted extends DomainEvent {

    public StockDepleted(String productId) {
        super(productId, "Product");
    }
}
