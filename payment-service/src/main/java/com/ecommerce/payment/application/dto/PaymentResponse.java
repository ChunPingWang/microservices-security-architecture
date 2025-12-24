package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.aggregates.Payment;
import com.ecommerce.payment.domain.value_objects.PaymentMethod;
import com.ecommerce.payment.domain.value_objects.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for payment details.
 */
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionId,
        Instant createdAt,
        Instant completedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency().getCurrencyCode(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getCreatedAt(),
                payment.getCompletedAt()
        );
    }
}
