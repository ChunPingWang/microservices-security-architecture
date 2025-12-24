package com.ecommerce.admin.infrastructure.web.controllers;

import com.ecommerce.admin.application.dto.DailySales;
import com.ecommerce.admin.application.dto.SalesReport;
import com.ecommerce.admin.application.dto.TopSellingProduct;
import com.ecommerce.admin.application.usecases.ReportUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for reports.
 */
@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    private final ReportUseCase reportUseCase;

    public ReportController(ReportUseCase reportUseCase) {
        this.reportUseCase = reportUseCase;
    }

    /**
     * Gets sales report for a date range.
     * GET /api/admin/reports/sales?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesReport> getSalesReport(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SalesReport report = reportUseCase.getSalesReport(adminId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Gets daily sales data for a date range.
     * GET /api/admin/reports/sales/daily?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/sales/daily")
    public ResponseEntity<List<DailySales>> getDailySales(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailySales> dailySales = reportUseCase.getDailySales(adminId, startDate, endDate);
        return ResponseEntity.ok(dailySales);
    }

    /**
     * Gets top selling products for a date range.
     * GET /api/admin/reports/products/top-selling?startDate=2024-01-01&endDate=2024-01-31&limit=10
     */
    @GetMapping("/products/top-selling")
    public ResponseEntity<List<TopSellingProduct>> getTopSellingProducts(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<TopSellingProduct> products = reportUseCase.getTopSellingProducts(
                adminId, startDate, endDate, limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Gets customer statistics for a date range.
     * GET /api/admin/reports/customers/statistics?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/customers/statistics")
    public ResponseEntity<Map<String, Object>> getCustomerStatistics(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        int newCustomers = reportUseCase.getNewCustomerCount(adminId, startDate, endDate);
        int activeCustomers = reportUseCase.getActiveCustomerCount(adminId, startDate, endDate);

        return ResponseEntity.ok(Map.of(
                "newCustomers", newCustomers,
                "activeCustomers", activeCustomers
        ));
    }
}
