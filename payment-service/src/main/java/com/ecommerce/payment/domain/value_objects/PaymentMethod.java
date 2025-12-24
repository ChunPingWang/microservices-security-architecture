package com.ecommerce.payment.domain.value_objects;

/**
 * Supported payment methods.
 */
public enum PaymentMethod {
    /**
     * Credit or debit card payment.
     */
    CREDIT_CARD,

    /**
     * LINE Pay mobile payment.
     */
    LINE_PAY,

    /**
     * Apple Pay.
     */
    APPLE_PAY,

    /**
     * Google Pay.
     */
    GOOGLE_PAY,

    /**
     * Bank transfer.
     */
    BANK_TRANSFER,

    /**
     * Cash on delivery.
     */
    CASH_ON_DELIVERY;

    /**
     * Check if this payment method requires online processing.
     */
    public boolean requiresOnlineProcessing() {
        return this != CASH_ON_DELIVERY;
    }

    /**
     * Check if this payment method is instant.
     */
    public boolean isInstant() {
        return this == CREDIT_CARD || this == LINE_PAY ||
               this == APPLE_PAY || this == GOOGLE_PAY;
    }
}
