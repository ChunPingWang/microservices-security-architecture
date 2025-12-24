package com.ecommerce.customer.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for membership information.
 */
public record MembershipResponse(
        UUID customerId,
        String memberLevel,
        BigDecimal totalSpending,
        BigDecimal spendingToNextLevel,
        int discountPercentage,
        String benefitDescription
) {}
