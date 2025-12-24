package com.ecommerce.order.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event raised when an order payment is completed.
 */
public class OrderPaid extends DomainEvent {

    private final UUID orderId;
    private final UUID customerId;
    private final UUID paymentId;
    private final BigDecimal amount;
    private final String currency;

    public OrderPaid(UUID orderId, UUID customerId, UUID paymentId,
                     BigDecimal amount, String currency) {
        super(orderId.toString(), "Order");
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
