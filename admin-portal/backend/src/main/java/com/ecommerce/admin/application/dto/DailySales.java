package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for daily sales data.
 */
public record DailySales(
        LocalDate date,
        BigDecimal revenue,
        int orderCount
) {}
