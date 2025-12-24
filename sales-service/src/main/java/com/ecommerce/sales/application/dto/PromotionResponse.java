package com.ecommerce.sales.application.dto;

import com.ecommerce.sales.domain.aggregates.Promotion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for promotion information.
 */
public record PromotionResponse(
        UUID id,
        String name,
        String description,
        String discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        Instant startDate,
        Instant endDate,
        boolean active
) {
    public static PromotionResponse from(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getDiscountRule().type().name(),
                promotion.getDiscountRule().value(),
                promotion.getDiscountRule().minimumOrderAmount() != null
                        ? promotion.getDiscountRule().minimumOrderAmount().getAmount()
                        : null,
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive()
        );
    }
}
