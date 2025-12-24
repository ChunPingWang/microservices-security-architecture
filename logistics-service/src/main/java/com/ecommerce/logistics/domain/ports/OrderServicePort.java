package com.ecommerce.logistics.domain.ports;

import java.util.UUID;

/**
 * Port for communicating with Order Service.
 */
public interface OrderServicePort {

    /**
     * Notifies the order service that a shipment was created.
     */
    void notifyShipmentCreated(UUID orderId, UUID shipmentId, String trackingNumber);

    /**
     * Notifies the order service that the shipment is in transit.
     */
    void notifyShipmentInTransit(UUID orderId, String trackingNumber);

    /**
     * Notifies the order service that the shipment was delivered.
     */
    void notifyShipmentDelivered(UUID orderId, String trackingNumber);

    /**
     * Notifies the order service that the shipment delivery failed.
     */
    void notifyShipmentFailed(UUID orderId, String trackingNumber, String reason);
}
