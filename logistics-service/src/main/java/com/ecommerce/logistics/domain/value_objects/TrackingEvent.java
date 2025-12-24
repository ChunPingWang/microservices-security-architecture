package com.ecommerce.logistics.domain.value_objects;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a tracking event in the shipment lifecycle.
 */
public record TrackingEvent(
        UUID id,
        String description,
        String location,
        Instant timestamp
) {
    public TrackingEvent {
        Objects.requireNonNull(id, "Event ID must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(timestamp, "Timestamp must not be null");
    }

    /**
     * Creates a new tracking event.
     */
    public static TrackingEvent create(String description, String location) {
        return new TrackingEvent(
                UUID.randomUUID(),
                description,
                location,
                Instant.now()
        );
    }
}
