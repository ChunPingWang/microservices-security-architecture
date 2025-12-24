package com.ecommerce.sales.application.exceptions;

/**
 * Exception thrown when a coupon is not valid.
 */
public class CouponNotValidException extends RuntimeException {

    private final String couponCode;
    private final String reason;

    public CouponNotValidException(String couponCode, String reason) {
        super("Coupon '" + couponCode + "' is not valid: " + reason);
        this.couponCode = couponCode;
        this.reason = reason;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getReason() {
        return reason;
    }
}
