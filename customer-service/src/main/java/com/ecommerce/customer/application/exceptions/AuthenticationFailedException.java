package com.ecommerce.customer.application.exceptions;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationFailedException extends RuntimeException {

    private final boolean accountLocked;

    public AuthenticationFailedException(String message) {
        super(message);
        this.accountLocked = false;
    }

    public AuthenticationFailedException(String message, boolean accountLocked) {
        super(message);
        this.accountLocked = accountLocked;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public static AuthenticationFailedException invalidCredentials() {
        return new AuthenticationFailedException("Invalid email or password");
    }

    public static AuthenticationFailedException accountLocked() {
        return new AuthenticationFailedException(
                "Account is locked due to too many failed attempts",
                true
        );
    }
}
