package com.ecommerce.payment.infrastructure.persistence.adapters;

import com.ecommerce.payment.domain.aggregates.Payment;
import com.ecommerce.payment.domain.ports.PaymentRepository;
import com.ecommerce.payment.domain.value_objects.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of PaymentRepository.
 * For development and testing purposes only.
 */
@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        payments.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return payments.values().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public List<Payment> findByCustomerId(UUID customerId) {
        return payments.values().stream()
                .filter(p -> p.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return payments.values().stream()
                .filter(p -> p.getStatus() == status)
                .toList();
    }

    @Override
    public List<Payment> findPendingPaymentsOlderThan(long timeoutMinutes) {
        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        return payments.values().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .filter(p -> p.getCreatedAt().isBefore(cutoff))
                .toList();
    }

    /**
     * Clears all payments. For testing purposes only.
     */
    public void clear() {
        payments.clear();
    }
}
