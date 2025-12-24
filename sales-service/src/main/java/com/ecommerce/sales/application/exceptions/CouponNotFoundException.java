package com.ecommerce.sales.application.exceptions;

/**
 * Exception thrown when a coupon is not found.
 */
public class CouponNotFoundException extends RuntimeException {

    private final String couponCode;

    public CouponNotFoundException(String couponCode) {
        super("Coupon not found: " + couponCode);
        this.couponCode = couponCode;
    }

    public String getCouponCode() {
        return couponCode;
    }
}
