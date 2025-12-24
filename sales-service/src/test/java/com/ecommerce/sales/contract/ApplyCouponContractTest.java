package com.ecommerce.sales.contract;

import com.ecommerce.sales.application.dto.ApplyCouponCommand;
import com.ecommerce.sales.application.dto.CouponValidationResponse;
import com.ecommerce.sales.application.exceptions.CouponNotFoundException;
import com.ecommerce.sales.application.exceptions.CouponNotValidException;
import com.ecommerce.sales.application.usecases.ApplyCouponUseCase;
import com.ecommerce.sales.infrastructure.web.CouponController;
import com.ecommerce.sales.infrastructure.web.GlobalExceptionHandler;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for POST /api/v1/coupons/validate endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CouponController.class, GlobalExceptionHandler.class})
@DisplayName("Apply Coupon Contract Tests")
class ApplyCouponContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplyCouponUseCase applyCouponUseCase;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid coupon code")
        void shouldAcceptValidCouponCode() throws Exception {
            CouponValidationResponse response = new CouponValidationResponse(
                    true,
                    "SAVE20",
                    "8折優惠券",
                    new BigDecimal("200.00"),
                    null
            );
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID))).thenReturn(response);

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "SAVE20",
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should require coupon code")
        void shouldRequireCouponCode() throws Exception {
            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should require order total")
        void shouldRequireOrderTotal() throws Exception {
            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "SAVE20"
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Response Contract - Valid Coupon")
    class ValidCouponContract {

        @Test
        @DisplayName("should return valid coupon with discount amount")
        void shouldReturnValidCouponWithDiscountAmount() throws Exception {
            CouponValidationResponse response = new CouponValidationResponse(
                    true,
                    "SAVE20",
                    "8折優惠券",
                    new BigDecimal("200.00"),
                    null
            );
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID))).thenReturn(response);

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "SAVE20",
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.couponCode").value("SAVE20"))
                    .andExpect(jsonPath("$.description").value("8折優惠券"))
                    .andExpect(jsonPath("$.discountAmount").value(200.00))
                    .andExpect(jsonPath("$.errorMessage").doesNotExist());
        }

        @Test
        @DisplayName("should return fixed amount discount")
        void shouldReturnFixedAmountDiscount() throws Exception {
            CouponValidationResponse response = new CouponValidationResponse(
                    true,
                    "FLAT100",
                    "折100元",
                    new BigDecimal("100.00"),
                    null
            );
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID))).thenReturn(response);

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "FLAT100",
                                    "orderTotal": 500.00
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.discountAmount").value(100.00));
        }
    }

    @Nested
    @DisplayName("Response Contract - Invalid Coupon")
    class InvalidCouponContract {

        @Test
        @DisplayName("should return error when coupon not found")
        void shouldReturnErrorWhenCouponNotFound() throws Exception {
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID)))
                    .thenThrow(new CouponNotFoundException("INVALID"));

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "INVALID",
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("COUPON_NOT_FOUND"));
        }

        @Test
        @DisplayName("should return error when coupon expired")
        void shouldReturnErrorWhenCouponExpired() throws Exception {
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID)))
                    .thenThrow(new CouponNotValidException("EXPIRED", "優惠券已過期"));

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "EXPIRED",
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("COUPON_NOT_VALID"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should return error when minimum order not met")
        void shouldReturnErrorWhenMinimumOrderNotMet() throws Exception {
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID)))
                    .thenThrow(new CouponNotValidException("MIN1000", "未達使用門檻，需滿 1000 元"));

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "MIN1000",
                                    "orderTotal": 500.00
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("COUPON_NOT_VALID"))
                    .andExpect(jsonPath("$.message").value("未達使用門檻，需滿 1000 元"));
        }

        @Test
        @DisplayName("should return error when coupon already used by customer")
        void shouldReturnErrorWhenCouponAlreadyUsedByCustomer() throws Exception {
            when(applyCouponUseCase.validate(any(), eq(CUSTOMER_ID)))
                    .thenThrow(new CouponNotValidException("USED", "您已使用過此優惠券"));

            mockMvc.perform(post("/api/v1/coupons/validate")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "USED",
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("COUPON_NOT_VALID"));
        }
    }

    @Nested
    @DisplayName("Apply Coupon Contract")
    class ApplyCouponToCartContract {

        @Test
        @DisplayName("should apply coupon to order")
        void shouldApplyCouponToOrder() throws Exception {
            CouponValidationResponse response = new CouponValidationResponse(
                    true,
                    "SAVE20",
                    "8折優惠券",
                    new BigDecimal("200.00"),
                    null
            );
            when(applyCouponUseCase.apply(any(), eq(CUSTOMER_ID))).thenReturn(response);

            mockMvc.perform(post("/api/v1/coupons/apply")
                            .header("X-Customer-Id", CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "couponCode": "SAVE20",
                                    "orderTotal": 1000.00
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.discountAmount").value(200.00));
        }
    }
}
