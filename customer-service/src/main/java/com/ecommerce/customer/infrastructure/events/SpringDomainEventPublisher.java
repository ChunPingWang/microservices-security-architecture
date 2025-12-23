package com.ecommerce.customer.infrastructure.events;

import com.ecommerce.shared.domain.events.DomainEvent;
import com.ecommerce.shared.domain.events.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-based implementation of DomainEventPublisher.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        logger.info("Publishing domain event: {} for aggregate: {}",
                event.getClass().getSimpleName(),
                event.getAggregateId());
        applicationEventPublisher.publishEvent(event);
    }
}
