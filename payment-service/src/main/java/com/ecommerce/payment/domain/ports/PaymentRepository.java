package com.ecommerce.payment.domain.ports;

import com.ecommerce.payment.domain.aggregates.Payment;
import com.ecommerce.payment.domain.value_objects.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Payment aggregate.
 */
public interface PaymentRepository {

    /**
     * Saves a payment.
     */
    Payment save(Payment payment);

    /**
     * Finds a payment by ID.
     */
    Optional<Payment> findById(UUID paymentId);

    /**
     * Finds a payment by order ID.
     */
    Optional<Payment> findByOrderId(UUID orderId);

    /**
     * Finds all payments by customer ID.
     */
    List<Payment> findByCustomerId(UUID customerId);

    /**
     * Finds payments by status.
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Finds payments that are pending and older than the timeout.
     */
    List<Payment> findPendingPaymentsOlderThan(long timeoutMinutes);
}
