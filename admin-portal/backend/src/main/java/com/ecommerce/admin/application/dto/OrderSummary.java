package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for order summary in admin portal.
 */
public record OrderSummary(
        UUID orderId,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {}
