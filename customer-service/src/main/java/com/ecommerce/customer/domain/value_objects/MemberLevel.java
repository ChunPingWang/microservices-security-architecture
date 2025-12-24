package com.ecommerce.customer.domain.value_objects;

import java.math.BigDecimal;

/**
 * Member level enumeration with spending thresholds.
 */
public enum MemberLevel {
    NORMAL(BigDecimal.ZERO),
    SILVER(new BigDecimal("10000")),
    GOLD(new BigDecimal("30000")),
    PLATINUM(new BigDecimal("100000"));

    private final BigDecimal threshold;

    MemberLevel(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    /**
     * Determines the member level based on total spending.
     *
     * @param totalSpending the total spending amount
     * @return the appropriate member level
     */
    public static MemberLevel fromSpending(BigDecimal totalSpending) {
        if (totalSpending.compareTo(PLATINUM.threshold) >= 0) {
            return PLATINUM;
        }
        if (totalSpending.compareTo(GOLD.threshold) >= 0) {
            return GOLD;
        }
        if (totalSpending.compareTo(SILVER.threshold) >= 0) {
            return SILVER;
        }
        return NORMAL;
    }

    /**
     * Calculates spending needed to reach next level.
     *
     * @param currentSpending current total spending
     * @return amount needed for next level, or zero if at PLATINUM
     */
    public BigDecimal spendingToNextLevel(BigDecimal currentSpending) {
        MemberLevel current = fromSpending(currentSpending);

        return switch (current) {
            case NORMAL -> SILVER.threshold.subtract(currentSpending);
            case SILVER -> GOLD.threshold.subtract(currentSpending);
            case GOLD -> PLATINUM.threshold.subtract(currentSpending);
            case PLATINUM -> BigDecimal.ZERO;
        };
    }
}
