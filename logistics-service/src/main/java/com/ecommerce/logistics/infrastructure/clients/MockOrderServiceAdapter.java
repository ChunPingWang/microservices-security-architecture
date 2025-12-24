package com.ecommerce.logistics.infrastructure.clients;

import com.ecommerce.logistics.domain.ports.OrderServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock order service adapter for development and testing.
 * In production, this would be replaced with a Feign client.
 */
@Component
public class MockOrderServiceAdapter implements OrderServicePort {

    private static final Logger log = LoggerFactory.getLogger(MockOrderServiceAdapter.class);

    @Override
    public void notifyShipmentCreated(UUID orderId, UUID shipmentId, String trackingNumber) {
        log.info("Notifying order service: shipment created for order {} with tracking {}",
                orderId, trackingNumber);
        // In production, this would call the order service API
    }

    @Override
    public void notifyShipmentInTransit(UUID orderId, String trackingNumber) {
        log.info("Notifying order service: shipment in transit for order {} with tracking {}",
                orderId, trackingNumber);
        // In production, this would call the order service API
    }

    @Override
    public void notifyShipmentDelivered(UUID orderId, String trackingNumber) {
        log.info("Notifying order service: shipment delivered for order {} with tracking {}",
                orderId, trackingNumber);
        // In production, this would call the order service API
    }

    @Override
    public void notifyShipmentFailed(UUID orderId, String trackingNumber, String reason) {
        log.info("Notifying order service: shipment failed for order {} - {}",
                orderId, reason);
        // In production, this would call the order service API
    }
}
