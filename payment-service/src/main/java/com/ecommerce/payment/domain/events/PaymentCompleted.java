package com.ecommerce.payment.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event raised when a payment is completed successfully.
 */
public class PaymentCompleted extends DomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final UUID customerId;
    private final String transactionId;
    private final BigDecimal amount;
    private final String currency;

    public PaymentCompleted(UUID paymentId, UUID orderId, UUID customerId,
                            String transactionId, BigDecimal amount, String currency) {
        super(paymentId.toString(), "Payment");
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
