package com.ecommerce.security.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

/**
 * Request-scoped context holder for current authenticated user.
 * Provides access to user information throughout the request lifecycle.
 */
@Component
@RequestScope
public class CurrentUserContext {

    private String userId;
    private String email;
    private String roles;
    private boolean authenticated;

    /**
     * Sets the current user information.
     */
    public void setCurrentUser(String userId, String email, String roles) {
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.authenticated = true;
    }

    /**
     * Clears the current user context.
     */
    public void clear() {
        this.userId = null;
        this.email = null;
        this.roles = null;
        this.authenticated = false;
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getRoles() {
        return Optional.ofNullable(roles);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Checks if the current user has the specified role.
     */
    public boolean hasRole(String role) {
        if (roles == null || role == null) {
            return false;
        }
        return roles.contains(role);
    }

    /**
     * Gets the current user ID or throws if not authenticated.
     */
    public String requireUserId() {
        return getUserId().orElseThrow(
            () -> new IllegalStateException("No authenticated user")
        );
    }
}
