package com.ecommerce.admin.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for sales report.
 */
public record SalesReport(
        BigDecimal totalRevenue,
        int totalOrders,
        BigDecimal averageOrderValue,
        LocalDate startDate,
        LocalDate endDate
) {}
