package com.ecommerce.logistics.application.usecases;

import com.ecommerce.logistics.application.dto.ShipmentResponse;
import com.ecommerce.logistics.application.exceptions.ShipmentNotFoundException;
import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.ports.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for tracking a shipment.
 */
@Service
public class TrackShipmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(TrackShipmentUseCase.class);

    private final ShipmentRepository shipmentRepository;

    public TrackShipmentUseCase(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Gets shipment tracking information by shipment ID.
     */
    public ShipmentResponse getById(UUID shipmentId) {
        log.debug("Tracking shipment by ID: {}", shipmentId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        return ShipmentResponse.from(shipment);
    }

    /**
     * Gets shipment tracking information by order ID.
     */
    public ShipmentResponse getByOrderId(UUID orderId) {
        log.debug("Tracking shipment by order ID: {}", orderId);

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("Order", orderId));

        return ShipmentResponse.from(shipment);
    }

    /**
     * Gets shipment tracking information by tracking number.
     */
    public ShipmentResponse getByTrackingNumber(String trackingNumber) {
        log.debug("Tracking shipment by tracking number: {}", trackingNumber);

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Tracking number", trackingNumber));

        return ShipmentResponse.from(shipment);
    }
}
