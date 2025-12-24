package com.ecommerce.logistics.domain.events;

import com.ecommerce.logistics.domain.value_objects.Carrier;
import com.ecommerce.shared.domain.events.DomainEvent;

import java.util.UUID;

/**
 * Domain event raised when a new shipment is created.
 */
public class ShipmentCreated extends DomainEvent {

    private final UUID shipmentId;
    private final UUID orderId;
    private final UUID customerId;
    private final String trackingNumber;
    private final Carrier carrier;

    public ShipmentCreated(UUID shipmentId, UUID orderId, UUID customerId,
                           String trackingNumber, Carrier carrier) {
        super(shipmentId.toString(), "Shipment");
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Carrier getCarrier() {
        return carrier;
    }
}
