package com.ecommerce.admin.contract;

import com.ecommerce.admin.application.dto.SalesReport;
import com.ecommerce.admin.application.dto.DailySales;
import com.ecommerce.admin.application.dto.TopSellingProduct;
import com.ecommerce.admin.application.usecases.ReportUseCase;
import com.ecommerce.admin.infrastructure.web.controllers.ReportController;
import com.ecommerce.admin.infrastructure.web.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Report endpoints.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ReportController.class, GlobalExceptionHandler.class})
@DisplayName("Report Contract Tests")
class ReportContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportUseCase reportUseCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Sales Report")
    class SalesReportTests {

        @Test
        @DisplayName("should return sales report")
        void shouldReturnSalesReport() throws Exception {
            SalesReport report = new SalesReport(
                    new BigDecimal("1500000"),
                    250,
                    new BigDecimal("6000"),
                    LocalDate.now().minusDays(30),
                    LocalDate.now()
            );
            when(reportUseCase.getSalesReport(any(), any(), any())).thenReturn(report);

            mockMvc.perform(get("/api/admin/reports/sales")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .param("startDate", LocalDate.now().minusDays(30).toString())
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRevenue").value(1500000))
                    .andExpect(jsonPath("$.totalOrders").value(250))
                    .andExpect(jsonPath("$.averageOrderValue").value(6000));
        }
    }

    @Nested
    @DisplayName("Daily Sales Report")
    class DailySalesReportTests {

        @Test
        @DisplayName("should return daily sales data")
        void shouldReturnDailySalesData() throws Exception {
            List<DailySales> dailySales = List.of(
                    new DailySales(LocalDate.now().minusDays(2), new BigDecimal("50000"), 10),
                    new DailySales(LocalDate.now().minusDays(1), new BigDecimal("75000"), 15),
                    new DailySales(LocalDate.now(), new BigDecimal("60000"), 12)
            );
            when(reportUseCase.getDailySales(any(), any(), any())).thenReturn(dailySales);

            mockMvc.perform(get("/api/admin/reports/sales/daily")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .param("startDate", LocalDate.now().minusDays(7).toString())
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].date").exists())
                    .andExpect(jsonPath("$[0].revenue").exists())
                    .andExpect(jsonPath("$[0].orderCount").exists());
        }
    }

    @Nested
    @DisplayName("Top Selling Products Report")
    class TopSellingProductsTests {

        @Test
        @DisplayName("should return top selling products")
        void shouldReturnTopSellingProducts() throws Exception {
            List<TopSellingProduct> topProducts = List.of(
                    new TopSellingProduct(UUID.randomUUID(), "iPhone 15", 150, new BigDecimal("5385000")),
                    new TopSellingProduct(UUID.randomUUID(), "MacBook Pro", 80, new BigDecimal("4792000")),
                    new TopSellingProduct(UUID.randomUUID(), "AirPods Pro", 200, new BigDecimal("1598000"))
            );
            when(reportUseCase.getTopSellingProducts(any(), any(), any(), any())).thenReturn(topProducts);

            mockMvc.perform(get("/api/admin/reports/products/top-selling")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .param("startDate", LocalDate.now().minusDays(30).toString())
                            .param("endDate", LocalDate.now().toString())
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].productId").exists())
                    .andExpect(jsonPath("$[0].productName").value("iPhone 15"))
                    .andExpect(jsonPath("$[0].quantitySold").value(150))
                    .andExpect(jsonPath("$[0].revenue").value(5385000));
        }
    }

    @Nested
    @DisplayName("Customer Statistics")
    class CustomerStatistics {

        @Test
        @DisplayName("should return customer statistics")
        void shouldReturnCustomerStatistics() throws Exception {
            when(reportUseCase.getNewCustomerCount(any(), any(), any())).thenReturn(125);
            when(reportUseCase.getActiveCustomerCount(any(), any(), any())).thenReturn(450);

            mockMvc.perform(get("/api/admin/reports/customers/statistics")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .param("startDate", LocalDate.now().minusDays(30).toString())
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.newCustomers").value(125))
                    .andExpect(jsonPath("$.activeCustomers").value(450));
        }
    }
}
