package com.ecommerce.customer.application.exceptions;

/**
 * Exception thrown when attempting to register with an existing email.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
