package com.ecommerce.payment.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event raised when a refund is initiated.
 */
public class RefundInitiated extends DomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final UUID customerId;
    private final BigDecimal refundAmount;
    private final String currency;
    private final String reason;

    public RefundInitiated(UUID paymentId, UUID orderId, UUID customerId,
                           BigDecimal refundAmount, String currency, String reason) {
        super(paymentId.toString(), "Payment");
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.reason = reason;
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

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReason() {
        return reason;
    }
}
