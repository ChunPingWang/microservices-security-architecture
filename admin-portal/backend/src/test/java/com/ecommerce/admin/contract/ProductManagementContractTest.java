package com.ecommerce.admin.contract;

import com.ecommerce.admin.application.dto.ProductSummary;
import com.ecommerce.admin.application.usecases.ProductManagementUseCase;
import com.ecommerce.admin.infrastructure.web.controllers.ProductManagementController;
import com.ecommerce.admin.infrastructure.web.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Product Management endpoints.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ProductManagementController.class, GlobalExceptionHandler.class})
@DisplayName("Product Management Contract Tests")
class ProductManagementContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductManagementUseCase productManagementUseCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("List Products")
    class ListProducts {

        @Test
        @DisplayName("should return list of products")
        void shouldReturnListOfProducts() throws Exception {
            List<ProductSummary> products = List.of(
                    new ProductSummary(UUID.randomUUID(), "SKU001", "iPhone 15", new BigDecimal("35900"), 100, true),
                    new ProductSummary(UUID.randomUUID(), "SKU002", "MacBook Pro", new BigDecimal("59900"), 50, true)
            );
            when(productManagementUseCase.listProducts(any())).thenReturn(products);

            mockMvc.perform(get("/api/admin/products")
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].productId").exists())
                    .andExpect(jsonPath("$[0].sku").value("SKU001"))
                    .andExpect(jsonPath("$[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$[0].price").value(35900))
                    .andExpect(jsonPath("$[0].stock").value(100))
                    .andExpect(jsonPath("$[0].active").value(true));
        }

        @Test
        @DisplayName("should return empty list when no products")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            when(productManagementUseCase.listProducts(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/products")
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Product Stock")
    class UpdateProductStock {

        @Test
        @DisplayName("should update product stock")
        void shouldUpdateProductStock() throws Exception {
            UUID productId = UUID.randomUUID();
            when(productManagementUseCase.updateStock(any(), any(), any())).thenReturn(true);

            mockMvc.perform(patch("/api/admin/products/{productId}/stock", productId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 150
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            UUID productId = UUID.randomUUID();
            when(productManagementUseCase.updateStock(any(), any(), any())).thenReturn(false);

            mockMvc.perform(patch("/api/admin/products/{productId}/stock", productId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 150
                                }
                                """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Toggle Product Status")
    class ToggleProductStatus {

        @Test
        @DisplayName("should activate product")
        void shouldActivateProduct() throws Exception {
            UUID productId = UUID.randomUUID();
            when(productManagementUseCase.toggleProductStatus(any(), any(), any())).thenReturn(true);

            mockMvc.perform(patch("/api/admin/products/{productId}/status", productId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": true
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should deactivate product")
        void shouldDeactivateProduct() throws Exception {
            UUID productId = UUID.randomUUID();
            when(productManagementUseCase.toggleProductStatus(any(), any(), any())).thenReturn(true);

            mockMvc.perform(patch("/api/admin/products/{productId}/status", productId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": false
                                }
                                """))
                    .andExpect(status().isOk());
        }
    }
}
