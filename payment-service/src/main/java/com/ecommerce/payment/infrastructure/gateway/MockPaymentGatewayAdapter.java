package com.ecommerce.payment.infrastructure.gateway;

import com.ecommerce.payment.domain.ports.PaymentGatewayPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock payment gateway for development and testing.
 * Simulates payment processing with configurable success/failure.
 */
@Component
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayAdapter.class);

    // Card number prefixes that trigger failures for testing
    private static final String FAIL_CARD_PREFIX = "4000";
    private static final String INSUFFICIENT_FUNDS_PREFIX = "4111";

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing payment: {} for amount: {}",
                request.paymentId(), request.amount());

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check for test failure conditions
        if (request.cardNumber() != null) {
            if (request.cardNumber().startsWith(FAIL_CARD_PREFIX)) {
                log.warn("Payment declined: Test failure card");
                return PaymentResult.failure("DECLINED", "Card declined by issuer");
            }
            if (request.cardNumber().startsWith(INSUFFICIENT_FUNDS_PREFIX)) {
                log.warn("Payment declined: Insufficient funds");
                return PaymentResult.failure("INSUFFICIENT_FUNDS", "Insufficient funds");
            }
        }

        // Generate successful transaction
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Payment successful: {}", transactionId);

        return PaymentResult.success(transactionId);
    }

    @Override
    public RefundResult processRefund(RefundRequest request) {
        log.info("Processing refund: {} for amount: {}",
                request.paymentId(), request.amount());

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate successful refund
        String refundTransactionId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Refund successful: {}", refundTransactionId);

        return RefundResult.success(refundTransactionId);
    }
}
