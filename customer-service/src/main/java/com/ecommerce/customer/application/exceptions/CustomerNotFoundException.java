package com.ecommerce.customer.application.exceptions;

/**
 * Exception thrown when a customer is not found.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public static CustomerNotFoundException byId(String id) {
        return new CustomerNotFoundException("Customer not found with id: " + id);
    }

    public static CustomerNotFoundException byEmail(String email) {
        return new CustomerNotFoundException("Customer not found with email: " + email);
    }
}
