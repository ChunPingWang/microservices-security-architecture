package com.ecommerce.logistics.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a shipment already exists for an order.
 */
public class ShipmentAlreadyExistsException extends RuntimeException {

    private final UUID orderId;

    public ShipmentAlreadyExistsException(UUID orderId) {
        super("Shipment already exists for order: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
