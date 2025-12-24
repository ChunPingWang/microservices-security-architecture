package com.ecommerce.order.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.util.UUID;

/**
 * Event raised when an order is cancelled.
 */
public class OrderCancelled extends DomainEvent {

    private final UUID orderId;
    private final UUID customerId;
    private final String reason;
    private final boolean requiresRefund;

    public OrderCancelled(UUID orderId, UUID customerId, String reason, boolean requiresRefund) {
        super(orderId.toString(), "Order");
        this.orderId = orderId;
        this.customerId = customerId;
        this.reason = reason;
        this.requiresRefund = requiresRefund;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getReason() {
        return reason;
    }

    public boolean isRequiresRefund() {
        return requiresRefund;
    }
}
