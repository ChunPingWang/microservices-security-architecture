package com.ecommerce.customer.domain.entities;

import com.ecommerce.customer.domain.events.CustomerRegistered;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import com.ecommerce.customer.domain.value_objects.Password;
import com.ecommerce.shared.domain.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Customer entity representing a registered user.
 * Aggregate root for customer-related operations.
 */
public class Customer {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UUID id;
    private final Email email;
    private Password password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private MemberLevel memberLevel;
    private BigDecimal totalSpending;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private long version;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Customer(
            UUID id,
            Email email,
            Password password,
            String firstName,
            String lastName,
            String phoneNumber
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.memberLevel = MemberLevel.NORMAL;
        this.totalSpending = BigDecimal.ZERO;
        this.failedLoginAttempts = 0;
        this.emailVerified = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    /**
     * Factory method for registering a new customer.
     */
    public static Customer register(
            Email email,
            Password password,
            String firstName,
            String lastName,
            String phoneNumber
    ) {
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(password, "Password is required");
        Objects.requireNonNull(firstName, "First name is required");
        Objects.requireNonNull(lastName, "Last name is required");

        Customer customer = new Customer(
            UUID.randomUUID(),
            email,
            password,
            firstName,
            lastName,
            phoneNumber
        );

        customer.domainEvents.add(new CustomerRegistered(
            customer.id.toString(),
            email.getValue(),
            firstName,
            lastName
        ));

        return customer;
    }

    /**
     * Reconstructs a customer from persistence.
     */
    public static Customer reconstitute(
            UUID id,
            Email email,
            Password password,
            String firstName,
            String lastName,
            String phoneNumber,
            MemberLevel memberLevel,
            BigDecimal totalSpending,
            int failedLoginAttempts,
            Instant lockedUntil,
            boolean emailVerified,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        Customer customer = new Customer(
            id, email, password, firstName, lastName, phoneNumber
        );
        customer.memberLevel = memberLevel;
        customer.totalSpending = totalSpending;
        customer.failedLoginAttempts = failedLoginAttempts;
        customer.lockedUntil = lockedUntil;
        customer.emailVerified = emailVerified;
        customer.createdAt = createdAt;
        customer.updatedAt = updatedAt;
        customer.version = version;
        return customer;
    }

    /**
     * Attempts to authenticate with the given password.
     */
    public boolean authenticate(String rawPassword) {
        if (isLocked()) {
            return false;
        }
        return password.matches(rawPassword);
    }

    /**
     * Records a failed login attempt.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES);
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Records a successful login, resetting failed attempts.
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the account is currently locked.
     */
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(lockedUntil)) {
            // Lock expired, reset
            this.lockedUntil = null;
            this.failedLoginAttempts = 0;
            return false;
        }
        return true;
    }

    /**
     * Adds spending and updates member level if needed.
     */
    public void addSpending(BigDecimal amount) {
        this.totalSpending = this.totalSpending.add(amount);
        this.memberLevel = MemberLevel.fromSpending(this.totalSpending);
        this.updatedAt = Instant.now();
    }

    /**
     * Verifies the customer's email.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates customer profile.
     */
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Changes the customer's password.
     */
    public void changePassword(Password newPassword) {
        Objects.requireNonNull(newPassword, "New password is required");
        this.password = newPassword;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public MemberLevel getMemberLevel() {
        return memberLevel;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
