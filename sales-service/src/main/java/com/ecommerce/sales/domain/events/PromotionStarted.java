package com.ecommerce.sales.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event raised when a promotion starts.
 */
public class PromotionStarted extends DomainEvent {

    private final UUID promotionId;
    private final String promotionName;
    private final Instant startDate;
    private final Instant endDate;

    public PromotionStarted(UUID promotionId, String promotionName,
                            Instant startDate, Instant endDate) {
        super(promotionId.toString(), "Promotion");
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getPromotionId() {
        return promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }
}
