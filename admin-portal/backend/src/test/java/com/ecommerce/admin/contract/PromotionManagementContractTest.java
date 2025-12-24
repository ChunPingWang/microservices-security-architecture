package com.ecommerce.admin.contract;

import com.ecommerce.admin.application.dto.PromotionSummary;
import com.ecommerce.admin.application.dto.CreatePromotionCommand;
import com.ecommerce.admin.application.usecases.PromotionManagementUseCase;
import com.ecommerce.admin.infrastructure.web.controllers.PromotionManagementController;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Promotion Management endpoints.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {PromotionManagementController.class, GlobalExceptionHandler.class})
@DisplayName("Promotion Management Contract Tests")
class PromotionManagementContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionManagementUseCase promotionManagementUseCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("List Promotions")
    class ListPromotions {

        @Test
        @DisplayName("should return list of promotions")
        void shouldReturnListOfPromotions() throws Exception {
            List<PromotionSummary> promotions = List.of(
                    new PromotionSummary(UUID.randomUUID(), "雙11特賣", "PERCENTAGE",
                            new BigDecimal("20"), true, LocalDateTime.now(), LocalDateTime.now().plusDays(7)),
                    new PromotionSummary(UUID.randomUUID(), "新會員優惠", "FIXED_AMOUNT",
                            new BigDecimal("100"), true, LocalDateTime.now(), LocalDateTime.now().plusDays(30))
            );
            when(promotionManagementUseCase.listPromotions(any())).thenReturn(promotions);

            mockMvc.perform(get("/api/admin/promotions")
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].promotionId").exists())
                    .andExpect(jsonPath("$[0].name").value("雙11特賣"))
                    .andExpect(jsonPath("$[0].discountType").value("PERCENTAGE"))
                    .andExpect(jsonPath("$[0].discountValue").value(20))
                    .andExpect(jsonPath("$[0].active").value(true));
        }
    }

    @Nested
    @DisplayName("Create Promotion")
    class CreatePromotion {

        @Test
        @DisplayName("should create promotion")
        void shouldCreatePromotion() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(promotionManagementUseCase.createPromotion(any(), any())).thenReturn(promotionId);

            mockMvc.perform(post("/api/admin/promotions")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "name": "聖誕特賣",
                                    "description": "全館商品8折優惠",
                                    "discountType": "PERCENTAGE",
                                    "discountValue": 20,
                                    "startDate": "2024-12-20T00:00:00",
                                    "endDate": "2024-12-31T23:59:59"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.promotionId").value(promotionId.toString()));
        }
    }

    @Nested
    @DisplayName("Toggle Promotion Status")
    class TogglePromotionStatus {

        @Test
        @DisplayName("should activate promotion")
        void shouldActivatePromotion() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(promotionManagementUseCase.togglePromotionStatus(any(), eq(promotionId), eq(true))).thenReturn(true);

            mockMvc.perform(patch("/api/admin/promotions/{promotionId}/status", promotionId)
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
        @DisplayName("should deactivate promotion")
        void shouldDeactivatePromotion() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(promotionManagementUseCase.togglePromotionStatus(any(), eq(promotionId), eq(false))).thenReturn(true);

            mockMvc.perform(patch("/api/admin/promotions/{promotionId}/status", promotionId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": false
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 404 when promotion not found")
        void shouldReturn404WhenPromotionNotFound() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(promotionManagementUseCase.togglePromotionStatus(any(), eq(promotionId), any())).thenReturn(false);

            mockMvc.perform(patch("/api/admin/promotions/{promotionId}/status", promotionId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": true
                                }
                                """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete Promotion")
    class DeletePromotion {

        @Test
        @DisplayName("should delete promotion")
        void shouldDeletePromotion() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(promotionManagementUseCase.deletePromotion(any(), eq(promotionId))).thenReturn(true);

            mockMvc.perform(delete("/api/admin/promotions/{promotionId}", promotionId)
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when promotion not found")
        void shouldReturn404WhenPromotionNotFound() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(promotionManagementUseCase.deletePromotion(any(), eq(promotionId))).thenReturn(false);

            mockMvc.perform(delete("/api/admin/promotions/{promotionId}", promotionId)
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isNotFound());
        }
    }
}
