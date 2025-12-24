package com.ecommerce.order.domain.value_objects;

/**
 * Represents the lifecycle status of an order.
 */
public enum OrderStatus {
    /**
     * Order created, waiting for payment.
     */
    PENDING_PAYMENT,

    /**
     * Payment has been completed.
     */
    PAID,

    /**
     * Order has been shipped.
     */
    SHIPPED,

    /**
     * Order has been delivered.
     */
    DELIVERED,

    /**
     * Order was cancelled before shipping.
     */
    CANCELLED,

    /**
     * Payment window expired.
     */
    PAYMENT_EXPIRED,

    /**
     * Order was refunded.
     */
    REFUNDED;

    /**
     * Check if the order can be cancelled in current status.
     */
    public boolean canCancel() {
        return this == PENDING_PAYMENT || this == PAID;
    }

    /**
     * Check if the order can be shipped in current status.
     */
    public boolean canShip() {
        return this == PAID;
    }

    /**
     * Check if the order can be marked as delivered in current status.
     */
    public boolean canDeliver() {
        return this == SHIPPED;
    }

    /**
     * Check if the order can be paid in current status.
     */
    public boolean canPay() {
        return this == PENDING_PAYMENT;
    }

    /**
     * Check if the payment can expire in current status.
     */
    public boolean canExpire() {
        return this == PENDING_PAYMENT;
    }
}
