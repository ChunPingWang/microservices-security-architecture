package com.ecommerce.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the e-commerce platform.
 * Domain events represent something significant that happened in the domain.
 */
public abstract class DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String aggregateId;
    private final String aggregateType;

    protected DomainEvent(String aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Returns the event type name for serialization and routing.
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
