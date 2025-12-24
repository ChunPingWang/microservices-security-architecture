package com.ecommerce.logistics.infrastructure.web.controllers;

import com.ecommerce.logistics.application.dto.CreateShipmentCommand;
import com.ecommerce.logistics.application.dto.ShipmentResponse;
import com.ecommerce.logistics.application.usecases.CreateShipmentUseCase;
import com.ecommerce.logistics.application.usecases.TrackShipmentUseCase;
import com.ecommerce.logistics.domain.ports.ShipmentRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for shipment operations.
 */
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;
    private final TrackShipmentUseCase trackShipmentUseCase;
    private final ShipmentRepository shipmentRepository;

    public ShipmentController(CreateShipmentUseCase createShipmentUseCase,
                               TrackShipmentUseCase trackShipmentUseCase,
                               ShipmentRepository shipmentRepository) {
        this.createShipmentUseCase = createShipmentUseCase;
        this.trackShipmentUseCase = trackShipmentUseCase;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Create a new shipment for an order.
     */
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentCommand command
    ) {
        ShipmentResponse response = createShipmentUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipment by ID.
     */
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(
            Principal principal,
            @PathVariable UUID shipmentId
    ) {
        ShipmentResponse response = trackShipmentUseCase.getById(shipmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipment tracking by order ID.
     */
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<ShipmentResponse> getTrackingByOrderId(
            Principal principal,
            @PathVariable UUID orderId
    ) {
        ShipmentResponse response = trackShipmentUseCase.getByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipment tracking by tracking number.
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getTrackingByTrackingNumber(
            Principal principal,
            @PathVariable String trackingNumber
    ) {
        ShipmentResponse response = trackShipmentUseCase.getByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all shipments for current customer.
     */
    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getShipments(Principal principal) {
        UUID customerId = UUID.fromString(principal.getName());
        List<ShipmentResponse> shipments = shipmentRepository.findByCustomerId(customerId)
                .stream()
                .map(ShipmentResponse::from)
                .toList();
        return ResponseEntity.ok(shipments);
    }
}
