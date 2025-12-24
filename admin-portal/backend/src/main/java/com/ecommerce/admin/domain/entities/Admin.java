package com.ecommerce.admin.domain.entities;

import com.ecommerce.admin.domain.value_objects.AdminRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Admin entity representing an administrator.
 */
public class Admin {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UUID id;
    private final String email;
    private String passwordHash;
    private String name;
    private AdminRole role;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private Admin(UUID id, String email, String passwordHash, String name, AdminRole role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Creates a new admin with password hashing.
     */
    public static Admin create(String email, String rawPassword, String name, AdminRole role) {
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(rawPassword, "Password is required");
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(role, "Role is required");

        String passwordHash = PASSWORD_ENCODER.encode(rawPassword);
        return new Admin(UUID.randomUUID(), email, passwordHash, name, role);
    }

    /**
     * Reconstitutes an admin from persistence.
     */
    public static Admin reconstitute(UUID id, String email, String passwordHash, String name,
                                      AdminRole role, boolean active, Instant createdAt,
                                      Instant updatedAt, Instant lastLoginAt) {
        Admin admin = new Admin(id, email, passwordHash, name, role);
        admin.active = active;
        admin.createdAt = createdAt;
        admin.updatedAt = updatedAt;
        admin.lastLoginAt = lastLoginAt;
        return admin;
    }

    /**
     * Authenticates with the given password.
     */
    public boolean authenticate(String rawPassword) {
        if (!active) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, passwordHash);
    }

    /**
     * Records a successful login.
     */
    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if admin has the specified permission.
     */
    public boolean hasPermission(String permission) {
        return role.hasPermission(permission);
    }

    /**
     * Deactivates the admin account.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Activates the admin account.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the admin's profile.
     */
    public void updateProfile(String name, AdminRole role) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (role != null) {
            this.role = role;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Changes the admin's password.
     */
    public void changePassword(String newRawPassword) {
        Objects.requireNonNull(newRawPassword, "New password is required");
        this.passwordHash = PASSWORD_ENCODER.encode(newRawPassword);
        this.updatedAt = Instant.now();
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public AdminRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Admin admin = (Admin) o;
        return id.equals(admin.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
