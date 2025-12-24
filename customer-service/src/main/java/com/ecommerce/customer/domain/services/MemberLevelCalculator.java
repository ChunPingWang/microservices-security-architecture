package com.ecommerce.customer.domain.services;

import com.ecommerce.customer.domain.value_objects.MemberLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Domain service for member level calculations and benefits.
 */
@Service
public class MemberLevelCalculator {

    /**
     * Calculates member level based on total spending.
     */
    public MemberLevel calculateLevel(BigDecimal totalSpending) {
        return MemberLevel.fromSpending(totalSpending);
    }

    /**
     * Calculates spending needed to reach the next level.
     */
    public BigDecimal spendingToNextLevel(BigDecimal currentSpending) {
        MemberLevel currentLevel = MemberLevel.fromSpending(currentSpending);

        return switch (currentLevel) {
            case NORMAL -> MemberLevel.SILVER.getThreshold().subtract(currentSpending);
            case SILVER -> MemberLevel.GOLD.getThreshold().subtract(currentSpending);
            case GOLD -> MemberLevel.PLATINUM.getThreshold().subtract(currentSpending);
            case PLATINUM -> BigDecimal.ZERO;
        };
    }

    /**
     * Gets the discount percentage for a member level.
     */
    public int getDiscountPercentage(MemberLevel level) {
        return switch (level) {
            case NORMAL -> 0;
            case SILVER -> 3;
            case GOLD -> 5;
            case PLATINUM -> 10;
        };
    }

    /**
     * Gets the benefit description for a member level.
     */
    public String getBenefitDescription(MemberLevel level, BigDecimal spendingToNext) {
        return switch (level) {
            case NORMAL -> String.format("歡迎加入！消費滿 %,.0f 元即可升級為銀卡會員", spendingToNext);
            case SILVER -> "銀卡會員專享折扣";
            case GOLD -> "金卡會員專享折扣及生日禮";
            case PLATINUM -> "尊榮白金會員專享10%折扣及優先服務";
        };
    }

    /**
     * Checks if spending would result in a level upgrade.
     */
    public boolean wouldUpgrade(MemberLevel currentLevel, BigDecimal newTotalSpending) {
        MemberLevel newLevel = MemberLevel.fromSpending(newTotalSpending);
        return newLevel.ordinal() > currentLevel.ordinal();
    }
}
