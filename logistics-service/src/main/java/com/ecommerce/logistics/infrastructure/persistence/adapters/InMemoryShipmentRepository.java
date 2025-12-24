package com.ecommerce.logistics.infrastructure.persistence.adapters;

import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.ports.ShipmentRepository;
import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ShipmentRepository.
 * For development and testing purposes only.
 */
@Repository
public class InMemoryShipmentRepository implements ShipmentRepository {

    private final Map<UUID, Shipment> shipments = new ConcurrentHashMap<>();

    @Override
    public Shipment save(Shipment shipment) {
        shipments.put(shipment.getId(), shipment);
        return shipment;
    }

    @Override
    public Optional<Shipment> findById(UUID shipmentId) {
        return Optional.ofNullable(shipments.get(shipmentId));
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return shipments.values().stream()
                .filter(s -> s.getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipments.values().stream()
                .filter(s -> s.getTrackingNumber().getValue().equals(trackingNumber))
                .findFirst();
    }

    @Override
    public List<Shipment> findByCustomerId(UUID customerId) {
        return shipments.values().stream()
                .filter(s -> s.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Shipment::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Shipment> findByStatus(DeliveryStatus status) {
        return shipments.values().stream()
                .filter(s -> s.getStatus() == status)
                .toList();
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return shipments.values().stream()
                .anyMatch(s -> s.getOrderId().equals(orderId));
    }

    /**
     * Clears all shipments. For testing purposes only.
     */
    public void clear() {
        shipments.clear();
    }
}
