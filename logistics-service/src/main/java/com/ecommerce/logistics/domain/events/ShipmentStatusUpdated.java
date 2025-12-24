package com.ecommerce.logistics.domain.events;

import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;
import com.ecommerce.shared.domain.events.DomainEvent;

import java.util.UUID;

/**
 * Domain event raised when a shipment status is updated.
 */
public class ShipmentStatusUpdated extends DomainEvent {

    private final UUID shipmentId;
    private final UUID orderId;
    private final UUID customerId;
    private final String trackingNumber;
    private final DeliveryStatus previousStatus;
    private final DeliveryStatus newStatus;
    private final String eventDescription;

    public ShipmentStatusUpdated(UUID shipmentId, UUID orderId, UUID customerId,
                                  String trackingNumber, DeliveryStatus previousStatus,
                                  DeliveryStatus newStatus, String eventDescription) {
        super(shipmentId.toString(), "Shipment");
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.trackingNumber = trackingNumber;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.eventDescription = eventDescription;
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

    public DeliveryStatus getPreviousStatus() {
        return previousStatus;
    }

    public DeliveryStatus getNewStatus() {
        return newStatus;
    }

    public String getEventDescription() {
        return eventDescription;
    }
}
