package com.ecommerce.logistics.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a shipment is not found.
 */
public class ShipmentNotFoundException extends RuntimeException {

    private final String identifier;

    public ShipmentNotFoundException(UUID shipmentId) {
        super("Shipment not found with ID: " + shipmentId);
        this.identifier = shipmentId.toString();
    }

    public ShipmentNotFoundException(String identifierType, UUID id) {
        super("Shipment not found with " + identifierType + ": " + id);
        this.identifier = id.toString();
    }

    public ShipmentNotFoundException(String identifierType, String value) {
        super("Shipment not found with " + identifierType + ": " + value);
        this.identifier = value;
    }

    public String getIdentifier() {
        return identifier;
    }
}
