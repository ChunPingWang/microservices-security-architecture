package com.ecommerce.payment.application.exceptions;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentFailedException extends RuntimeException {

    private final String errorCode;

    public PaymentFailedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
