package com.ecommerce.order.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event raised when a new order is created.
 */
public class OrderCreated extends DomainEvent {

    private final UUID orderId;
    private final UUID customerId;
    private final int itemCount;
    private final BigDecimal totalAmount;
    private final String currency;

    public OrderCreated(UUID orderId, UUID customerId, int itemCount,
                        BigDecimal totalAmount, String currency) {
        super(orderId.toString(), "Order");
        this.orderId = orderId;
        this.customerId = customerId;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
        this.currency = currency;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }
}
