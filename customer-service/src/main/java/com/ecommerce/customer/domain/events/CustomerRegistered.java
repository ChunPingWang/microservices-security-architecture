package com.ecommerce.customer.domain.events;

import com.ecommerce.shared.domain.events.DomainEvent;

/**
 * Domain event emitted when a new customer registers.
 */
public final class CustomerRegistered extends DomainEvent {

    private final String email;
    private final String firstName;
    private final String lastName;

    public CustomerRegistered(
            String customerId,
            String email,
            String firstName,
            String lastName
    ) {
        super(customerId, "Customer");
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
