package com.ecommerce.sales.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a promotion is not found.
 */
public class PromotionNotFoundException extends RuntimeException {

    private final UUID promotionId;

    public PromotionNotFoundException(UUID promotionId) {
        super("Promotion not found: " + promotionId);
        this.promotionId = promotionId;
    }

    public UUID getPromotionId() {
        return promotionId;
    }
}
