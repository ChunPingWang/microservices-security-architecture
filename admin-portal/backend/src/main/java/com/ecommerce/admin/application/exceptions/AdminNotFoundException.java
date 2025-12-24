package com.ecommerce.admin.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when admin is not found.
 */
public class AdminNotFoundException extends RuntimeException {

    private final UUID adminId;

    public AdminNotFoundException(UUID adminId) {
        super("Admin not found: " + adminId);
        this.adminId = adminId;
    }

    public UUID getAdminId() {
        return adminId;
    }
}
