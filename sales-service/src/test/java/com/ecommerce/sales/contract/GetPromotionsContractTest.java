package com.ecommerce.sales.contract;

import com.ecommerce.sales.application.dto.PromotionResponse;
import com.ecommerce.sales.application.usecases.GetPromotionsUseCase;
import com.ecommerce.sales.domain.aggregates.Promotion;
import com.ecommerce.sales.domain.value_objects.DiscountRule;
import com.ecommerce.sales.domain.value_objects.DiscountType;
import com.ecommerce.sales.infrastructure.web.PromotionController;
import com.ecommerce.sales.infrastructure.web.GlobalExceptionHandler;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/promotions endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {PromotionController.class, GlobalExceptionHandler.class})
@DisplayName("Get Promotions Contract Tests")
class GetPromotionsContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetPromotionsUseCase getPromotionsUseCase;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Response Contract - Promotion List")
    class PromotionListContract {

        @Test
        @DisplayName("should return empty list when no promotions")
        void shouldReturnEmptyListWhenNoPromotions() throws Exception {
            when(getPromotionsUseCase.getActivePromotions()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/promotions")
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("should return list of promotions with all required fields")
        void shouldReturnPromotionListWithRequiredFields() throws Exception {
            PromotionResponse promotion = createPromotionResponse();
            when(getPromotionsUseCase.getActivePromotions()).thenReturn(List.of(promotion));

            mockMvc.perform(get("/api/v1/promotions")
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].description").exists())
                    .andExpect(jsonPath("$[0].discountType").exists())
                    .andExpect(jsonPath("$[0].discountValue").isNumber())
                    .andExpect(jsonPath("$[0].startDate").exists())
                    .andExpect(jsonPath("$[0].endDate").exists())
                    .andExpect(jsonPath("$[0].active").isBoolean());
        }

        @Test
        @DisplayName("should return multiple promotions")
        void shouldReturnMultiplePromotions() throws Exception {
            PromotionResponse promo1 = createPromotionResponse();
            PromotionResponse promo2 = createPromotionResponse();
            when(getPromotionsUseCase.getActivePromotions()).thenReturn(List.of(promo1, promo2));

            mockMvc.perform(get("/api/v1/promotions")
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("Response Contract - Single Promotion")
    class SinglePromotionContract {

        @Test
        @DisplayName("should return promotion by ID")
        void shouldReturnPromotionById() throws Exception {
            UUID promotionId = UUID.randomUUID();
            PromotionResponse promotion = createPromotionResponseWithId(promotionId);
            when(getPromotionsUseCase.getById(promotionId)).thenReturn(promotion);

            mockMvc.perform(get("/api/v1/promotions/{promotionId}", promotionId)
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(promotionId.toString()))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.discountType").exists());
        }

        @Test
        @DisplayName("should return 404 when promotion not found")
        void shouldReturn404WhenPromotionNotFound() throws Exception {
            UUID promotionId = UUID.randomUUID();
            when(getPromotionsUseCase.getById(promotionId))
                    .thenThrow(new com.ecommerce.sales.application.exceptions.PromotionNotFoundException(promotionId));

            mockMvc.perform(get("/api/v1/promotions/{promotionId}", promotionId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Response Contract - Discount Type Display")
    class DiscountTypeDisplayContract {

        @Test
        @DisplayName("should display percentage discount correctly")
        void shouldDisplayPercentageDiscountCorrectly() throws Exception {
            PromotionResponse promotion = new PromotionResponse(
                    UUID.randomUUID(),
                    "8折優惠",
                    "全站商品8折",
                    "PERCENTAGE",
                    new BigDecimal("20"),
                    null,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    true
            );
            when(getPromotionsUseCase.getActivePromotions()).thenReturn(List.of(promotion));

            mockMvc.perform(get("/api/v1/promotions")
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].discountType").value("PERCENTAGE"))
                    .andExpect(jsonPath("$[0].discountValue").value(20));
        }

        @Test
        @DisplayName("should display fixed amount discount correctly")
        void shouldDisplayFixedAmountDiscountCorrectly() throws Exception {
            PromotionResponse promotion = new PromotionResponse(
                    UUID.randomUUID(),
                    "折100",
                    "訂單折100元",
                    "FIXED_AMOUNT",
                    new BigDecimal("100"),
                    null,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    true
            );
            when(getPromotionsUseCase.getActivePromotions()).thenReturn(List.of(promotion));

            mockMvc.perform(get("/api/v1/promotions")
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].discountType").value("FIXED_AMOUNT"))
                    .andExpect(jsonPath("$[0].discountValue").value(100));
        }
    }

    private PromotionResponse createPromotionResponse() {
        return new PromotionResponse(
                UUID.randomUUID(),
                "雙11特賣",
                "全站商品9折優惠",
                "PERCENTAGE",
                new BigDecimal("10"),
                null,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                true
        );
    }

    private PromotionResponse createPromotionResponseWithId(UUID id) {
        return new PromotionResponse(
                id,
                "雙11特賣",
                "全站商品9折優惠",
                "PERCENTAGE",
                new BigDecimal("10"),
                null,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                true
        );
    }
}
