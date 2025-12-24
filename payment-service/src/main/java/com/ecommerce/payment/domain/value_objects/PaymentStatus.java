package com.ecommerce.payment.domain.value_objects;

/**
 * Represents the status of a payment.
 */
public enum PaymentStatus {
    /**
     * Payment created, waiting for processing.
     */
    PENDING,

    /**
     * Payment is being processed by the gateway.
     */
    PROCESSING,

    /**
     * Payment completed successfully.
     */
    COMPLETED,

    /**
     * Payment failed.
     */
    FAILED,

    /**
     * Payment was fully refunded.
     */
    REFUNDED,

    /**
     * Payment was partially refunded.
     */
    PARTIALLY_REFUNDED,

    /**
     * Payment expired before completion.
     */
    EXPIRED;

    /**
     * Check if the payment can be processed.
     */
    public boolean canProcess() {
        return this == PENDING;
    }

    /**
     * Check if the payment can be completed.
     */
    public boolean canComplete() {
        return this == PROCESSING;
    }

    /**
     * Check if the payment can be refunded.
     */
    public boolean canRefund() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }

    /**
     * Check if the payment can expire.
     */
    public boolean canExpire() {
        return this == PENDING || this == PROCESSING;
    }
}
