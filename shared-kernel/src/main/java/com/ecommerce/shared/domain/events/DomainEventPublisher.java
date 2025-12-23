package com.ecommerce.shared.domain.events;

/**
 * Interface for publishing domain events.
 */
public interface DomainEventPublisher {

    /**
     * Publishes a domain event.
     *
     * @param event the event to publish
     */
    void publish(DomainEvent event);
}
