package com.ecommerce.payment.domain.aggregates;

import com.ecommerce.payment.domain.value_objects.PaymentMethod;
import com.ecommerce.payment.domain.value_objects.PaymentStatus;
import com.ecommerce.shared.domain.value_objects.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Payment aggregate root.
 * Manages the lifecycle of a payment from creation to completion/refund.
 */
public class Payment {

    private final UUID id;
    private final UUID orderId;
    private final UUID customerId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private Money refundedAmount;
    private String refundReason;
    private final Instant createdAt;
    private Instant completedAt;
    private Instant refundedAt;
    private Instant updatedAt;

    private Payment(UUID id, UUID orderId, UUID customerId, Money amount,
                    PaymentMethod paymentMethod, PaymentStatus status,
                    Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.refundedAmount = Money.zero();
    }

    /**
     * Creates a new payment.
     */
    public static Payment create(UUID orderId, UUID customerId, Money amount,
                                  PaymentMethod paymentMethod) {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(paymentMethod, "Payment method must not be null");

        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        return new Payment(
                UUID.randomUUID(),
                orderId,
                customerId,
                amount,
                paymentMethod,
                PaymentStatus.PENDING,
                Instant.now()
        );
    }

    /**
     * Reconstitutes a payment from persistence.
     */
    public static Payment reconstitute(UUID id, UUID orderId, UUID customerId,
                                        Money amount, PaymentMethod paymentMethod,
                                        PaymentStatus status, String transactionId,
                                        String failureReason, Money refundedAmount,
                                        String refundReason, Instant createdAt,
                                        Instant completedAt, Instant refundedAt,
                                        Instant updatedAt) {
        Payment payment = new Payment(id, orderId, customerId, amount,
                paymentMethod, status, createdAt);
        payment.transactionId = transactionId;
        payment.failureReason = failureReason;
        payment.refundedAmount = refundedAmount != null ? refundedAmount : Money.zero();
        payment.refundReason = refundReason;
        payment.completedAt = completedAt;
        payment.refundedAt = refundedAt;
        payment.updatedAt = updatedAt;
        return payment;
    }

    /**
     * Starts processing the payment.
     */
    public void startProcessing() {
        if (!status.canProcess()) {
            throw new IllegalStateException(
                    "Cannot start processing. Current status: " + status);
        }

        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    /**
     * Completes the payment successfully.
     */
    public void complete(String transactionId) {
        if (!status.canComplete()) {
            throw new IllegalStateException(
                    "Cannot complete payment. Current status: " + status);
        }
        Objects.requireNonNull(transactionId, "Transaction ID must not be null");

        this.transactionId = transactionId;
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Fails the payment.
     */
    public void fail(String reason) {
        if (status != PaymentStatus.PROCESSING && status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot fail payment. Current status: " + status);
        }
        Objects.requireNonNull(reason, "Failure reason must not be null");

        this.failureReason = reason;
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    /**
     * Refunds the payment (full or partial).
     */
    public void refund(Money refundAmount, String reason) {
        if (!status.canRefund()) {
            throw new IllegalStateException(
                    "Cannot refund payment. Current status: " + status);
        }
        Objects.requireNonNull(refundAmount, "Refund amount must not be null");
        Objects.requireNonNull(reason, "Refund reason must not be null");

        Money totalRefunded = this.refundedAmount.add(refundAmount);
        if (totalRefunded.getAmount().compareTo(amount.getAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount cannot exceed original payment amount");
        }

        this.refundedAmount = totalRefunded;
        this.refundReason = reason;
        this.refundedAt = Instant.now();
        this.updatedAt = Instant.now();

        if (totalRefunded.getAmount().compareTo(amount.getAmount()) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    /**
     * Expires the payment.
     */
    public void expire() {
        if (!status.canExpire()) {
            throw new IllegalStateException(
                    "Cannot expire payment. Current status: " + status);
        }

        this.status = PaymentStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Money getRefundedAmount() {
        return refundedAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", amount=" + amount +
                ", status=" + status +
                ", paymentMethod=" + paymentMethod +
                '}';
    }
}
