package com.ecommerce.customer.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;

/**
 * Domain event raised when a customer's member level is upgraded.
 */
public class LevelUpgraded extends DomainEvent {

    private final String previousLevel;
    private final String newLevel;
    private final int newDiscountPercentage;
    private final BigDecimal totalSpending;

    public LevelUpgraded(String customerId, String previousLevel, String newLevel,
                         int newDiscountPercentage, BigDecimal totalSpending) {
        super(customerId, "Customer");
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.newDiscountPercentage = newDiscountPercentage;
        this.totalSpending = totalSpending;
    }

    public String getCustomerId() {
        return getAggregateId();
    }

    public String getPreviousLevel() {
        return previousLevel;
    }

    public String getNewLevel() {
        return newLevel;
    }

    public int getNewDiscountPercentage() {
        return newDiscountPercentage;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }
}
