package com.ecommerce.sales.domain.value_objects;

import com.ecommerce.shared.domain.value_objects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a discount rule.
 */
public record DiscountRule(
        DiscountType type,
        BigDecimal value,
        Money minimumOrderAmount
) {
    public DiscountRule {
        Objects.requireNonNull(type, "Discount type must not be null");
        Objects.requireNonNull(value, "Discount value must not be null");

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount value cannot be negative");
        }

        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }
    }

    /**
     * Creates a percentage discount rule.
     */
    public static DiscountRule percentage(int percentage) {
        return new DiscountRule(
                DiscountType.PERCENTAGE,
                new BigDecimal(percentage),
                null
        );
    }

    /**
     * Creates a percentage discount rule with minimum order amount.
     */
    public static DiscountRule percentageWithMinimum(int percentage, Money minimumOrderAmount) {
        return new DiscountRule(
                DiscountType.PERCENTAGE,
                new BigDecimal(percentage),
                minimumOrderAmount
        );
    }

    /**
     * Creates a fixed amount discount rule.
     */
    public static DiscountRule fixedAmount(Money amount) {
        return new DiscountRule(
                DiscountType.FIXED_AMOUNT,
                amount.getAmount(),
                null
        );
    }

    /**
     * Creates a fixed amount discount rule with minimum order amount.
     */
    public static DiscountRule fixedAmountWithMinimum(Money amount, Money minimumOrderAmount) {
        return new DiscountRule(
                DiscountType.FIXED_AMOUNT,
                amount.getAmount(),
                minimumOrderAmount
        );
    }

    /**
     * Calculates the discount amount for a given order total.
     */
    public Money calculateDiscount(Money orderTotal) {
        if (minimumOrderAmount != null &&
                orderTotal.getAmount().compareTo(minimumOrderAmount.getAmount()) < 0) {
            return Money.zero();
        }

        if (type == DiscountType.PERCENTAGE) {
            BigDecimal discountAmount = orderTotal.getAmount()
                    .multiply(value)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            return Money.of(discountAmount);
        } else {
            // Fixed amount - cap at order total
            if (value.compareTo(orderTotal.getAmount()) > 0) {
                return orderTotal;
            }
            return Money.of(value);
        }
    }

    /**
     * Checks if the order meets the minimum requirement.
     */
    public boolean meetsMinimum(Money orderTotal) {
        if (minimumOrderAmount == null) {
            return true;
        }
        return orderTotal.getAmount().compareTo(minimumOrderAmount.getAmount()) >= 0;
    }

    /**
     * Gets the display text for this discount rule.
     */
    public String getDisplayText() {
        if (type == DiscountType.PERCENTAGE) {
            return value.intValue() + "% 折扣";
        } else {
            return "折 " + value.setScale(0, RoundingMode.DOWN) + " 元";
        }
    }
}
