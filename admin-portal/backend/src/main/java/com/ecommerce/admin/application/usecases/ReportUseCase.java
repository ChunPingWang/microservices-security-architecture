package com.ecommerce.admin.application.usecases;

import com.ecommerce.admin.application.dto.DailySales;
import com.ecommerce.admin.application.dto.SalesReport;
import com.ecommerce.admin.application.dto.TopSellingProduct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Use case for generating reports.
 * Note: In a real implementation, this would aggregate data from multiple services.
 */
@Service
public class ReportUseCase {

    /**
     * Gets sales report for a date range.
     */
    public SalesReport getSalesReport(UUID adminId, LocalDate startDate, LocalDate endDate) {
        // Mock implementation
        BigDecimal totalRevenue = new BigDecimal("1500000");
        int totalOrders = 250;
        BigDecimal averageOrderValue = totalRevenue.divide(
                BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        return new SalesReport(totalRevenue, totalOrders, averageOrderValue, startDate, endDate);
    }

    /**
     * Gets daily sales data for a date range.
     */
    public List<DailySales> getDailySales(UUID adminId, LocalDate startDate, LocalDate endDate) {
        // Mock implementation - generate sample data
        List<DailySales> dailySales = new ArrayList<>();
        LocalDate current = startDate;
        Random random = new Random(42); // Fixed seed for consistent results

        while (!current.isAfter(endDate)) {
            BigDecimal revenue = BigDecimal.valueOf(30000 + random.nextInt(70000));
            int orderCount = 5 + random.nextInt(20);
            dailySales.add(new DailySales(current, revenue, orderCount));
            current = current.plusDays(1);
        }

        return dailySales;
    }

    /**
     * Gets top selling products for a date range.
     */
    public List<TopSellingProduct> getTopSellingProducts(UUID adminId, LocalDate startDate,
                                                          LocalDate endDate, Integer limit) {
        // Mock implementation
        return List.of(
                new TopSellingProduct(UUID.randomUUID(), "iPhone 15", 150, new BigDecimal("5385000")),
                new TopSellingProduct(UUID.randomUUID(), "MacBook Pro", 80, new BigDecimal("4792000")),
                new TopSellingProduct(UUID.randomUUID(), "AirPods Pro", 200, new BigDecimal("1598000")),
                new TopSellingProduct(UUID.randomUUID(), "iPad Air", 120, new BigDecimal("2388000")),
                new TopSellingProduct(UUID.randomUUID(), "Apple Watch", 180, new BigDecimal("2340000"))
        ).stream()
                .limit(limit != null ? limit : 10)
                .toList();
    }

    /**
     * Gets new customer count for a date range.
     */
    public int getNewCustomerCount(UUID adminId, LocalDate startDate, LocalDate endDate) {
        // Mock implementation
        return 125;
    }

    /**
     * Gets active customer count for a date range.
     */
    public int getActiveCustomerCount(UUID adminId, LocalDate startDate, LocalDate endDate) {
        // Mock implementation
        return 450;
    }
}
