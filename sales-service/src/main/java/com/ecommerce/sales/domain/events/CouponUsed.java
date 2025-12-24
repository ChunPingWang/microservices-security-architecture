package com.ecommerce.sales.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain event raised when a coupon is used.
 */
public class CouponUsed extends DomainEvent {

    private final UUID couponId;
    private final String couponCode;
    private final UUID customerId;
    private final UUID orderId;
    private final BigDecimal discountAmount;

    public CouponUsed(UUID couponId, String couponCode, UUID customerId,
                      UUID orderId, BigDecimal discountAmount) {
        super(couponId.toString(), "Coupon");
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.customerId = customerId;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
}
