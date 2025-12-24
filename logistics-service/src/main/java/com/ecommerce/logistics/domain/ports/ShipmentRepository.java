package com.ecommerce.logistics.domain.ports;

import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Shipment aggregate.
 */
public interface ShipmentRepository {

    /**
     * Saves a shipment.
     */
    Shipment save(Shipment shipment);

    /**
     * Finds a shipment by ID.
     */
    Optional<Shipment> findById(UUID shipmentId);

    /**
     * Finds a shipment by order ID.
     */
    Optional<Shipment> findByOrderId(UUID orderId);

    /**
     * Finds a shipment by tracking number.
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Finds all shipments for a customer.
     */
    List<Shipment> findByCustomerId(UUID customerId);

    /**
     * Finds shipments by status.
     */
    List<Shipment> findByStatus(DeliveryStatus status);

    /**
     * Checks if a shipment exists.
     */
    boolean existsByOrderId(UUID orderId);
}
