package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for customer summary in admin portal.
 */
public record CustomerSummary(
        UUID customerId,
        String email,
        String name,
        String memberLevel,
        BigDecimal totalSpending,
        boolean active,
        LocalDateTime createdAt
) {}
