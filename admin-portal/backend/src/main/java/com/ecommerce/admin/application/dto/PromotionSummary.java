package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for promotion summary in admin portal.
 */
public record PromotionSummary(
        UUID promotionId,
        String name,
        String discountType,
        BigDecimal discountValue,
        boolean active,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
