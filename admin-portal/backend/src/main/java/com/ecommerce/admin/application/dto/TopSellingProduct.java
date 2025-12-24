package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for top selling product report.
 */
public record TopSellingProduct(
        UUID productId,
        String productName,
        int quantitySold,
        BigDecimal revenue
) {}
