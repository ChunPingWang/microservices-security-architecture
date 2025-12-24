package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command for creating a promotion.
 */
public record CreatePromotionCommand(
        String name,
        String description,
        String discountType,
        BigDecimal discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
