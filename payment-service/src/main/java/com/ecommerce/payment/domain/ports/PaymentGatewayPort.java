package com.ecommerce.payment.domain.ports;

import com.ecommerce.payment.domain.value_objects.PaymentMethod;
import com.ecommerce.shared.domain.value_objects.Money;

import java.util.UUID;

/**
 * Port for payment gateway abstraction.
 */
public interface PaymentGatewayPort {

    /**
     * Processes a payment through the gateway.
     */
    PaymentResult processPayment(PaymentRequest request);

    /**
     * Processes a refund through the gateway.
     */
    RefundResult processRefund(RefundRequest request);

    /**
     * Payment request data.
     */
    record PaymentRequest(
            UUID paymentId,
            Money amount,
            PaymentMethod paymentMethod,
            String cardNumber,
            String cardHolderName,
            String expiryMonth,
            String expiryYear,
            String cvv
    ) {}

    /**
     * Payment processing result.
     */
    record PaymentResult(
            boolean success,
            String transactionId,
            String errorCode,
            String errorMessage
    ) {
        public static PaymentResult success(String transactionId) {
            return new PaymentResult(true, transactionId, null, null);
        }

        public static PaymentResult failure(String errorCode, String errorMessage) {
            return new PaymentResult(false, null, errorCode, errorMessage);
        }
    }

    /**
     * Refund request data.
     */
    record RefundRequest(
            UUID paymentId,
            String transactionId,
            Money amount,
            String reason
    ) {}

    /**
     * Refund processing result.
     */
    record RefundResult(
            boolean success,
            String refundTransactionId,
            String errorCode,
            String errorMessage
    ) {
        public static RefundResult success(String refundTransactionId) {
            return new RefundResult(true, refundTransactionId, null, null);
        }

        public static RefundResult failure(String errorCode, String errorMessage) {
            return new RefundResult(false, null, errorCode, errorMessage);
        }
    }
}
