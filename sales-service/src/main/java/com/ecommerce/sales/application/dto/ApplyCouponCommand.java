package com.ecommerce.sales.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Command for applying/validating a coupon.
 */
public record ApplyCouponCommand(
        @NotBlank(message = "Coupon code is required")
        String couponCode,

        @NotNull(message = "Order total is required")
        @Positive(message = "Order total must be positive")
        BigDecimal orderTotal
) {}
