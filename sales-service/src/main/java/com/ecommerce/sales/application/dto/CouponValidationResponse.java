package com.ecommerce.sales.application.dto;

import java.math.BigDecimal;

/**
 * Response DTO for coupon validation result.
 */
public record CouponValidationResponse(
        boolean valid,
        String couponCode,
        String description,
        BigDecimal discountAmount,
        String errorMessage
) {
    public static CouponValidationResponse valid(String couponCode, String description, BigDecimal discountAmount) {
        return new CouponValidationResponse(true, couponCode, description, discountAmount, null);
    }

    public static CouponValidationResponse invalid(String couponCode, String errorMessage) {
        return new CouponValidationResponse(false, couponCode, null, null, errorMessage);
    }
}
