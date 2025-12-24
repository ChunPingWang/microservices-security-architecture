package com.ecommerce.admin.application.exceptions;

/**
 * Exception thrown when admin authentication fails.
 */
public class AdminAuthenticationException extends RuntimeException {

    public AdminAuthenticationException(String message) {
        super(message);
    }
}
