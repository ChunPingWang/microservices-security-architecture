package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary DTO for product listing in admin portal.
 */
public record ProductSummary(
        UUID productId,
        String sku,
        String name,
        BigDecimal price,
        int stock,
        boolean active
) {}
