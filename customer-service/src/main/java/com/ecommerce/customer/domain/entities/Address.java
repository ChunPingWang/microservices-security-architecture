package com.ecommerce.customer.domain.entities;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Address entity for customer shipping addresses.
 */
public class Address {

    private final UUID id;
    private final UUID customerId;
    private String recipientName;
    private String phoneNumber;
    private String postalCode;
    private String city;
    private String district;
    private String street;
    private String country;
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;

    private Address(
            UUID id,
            UUID customerId,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String city,
            String district,
            String street,
            String country,
            boolean isDefault
    ) {
        this.id = id;
        this.customerId = customerId;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.district = district;
        this.street = street;
        this.country = country;
        this.isDefault = isDefault;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Creates a new address for a customer.
     */
    public static Address create(
            UUID customerId,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String city,
            String district,
            String street,
            String country,
            boolean isDefault
    ) {
        Objects.requireNonNull(customerId, "Customer ID is required");
        Objects.requireNonNull(recipientName, "Recipient name is required");
        Objects.requireNonNull(phoneNumber, "Phone number is required");
        Objects.requireNonNull(postalCode, "Postal code is required");
        Objects.requireNonNull(city, "City is required");
        Objects.requireNonNull(street, "Street is required");

        return new Address(
            UUID.randomUUID(),
            customerId,
            recipientName,
            phoneNumber,
            postalCode,
            city,
            district,
            street,
            country != null ? country : "Taiwan",
            isDefault
        );
    }

    /**
     * Reconstructs an address from persistence.
     */
    public static Address reconstitute(
            UUID id,
            UUID customerId,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String city,
            String district,
            String street,
            String country,
            boolean isDefault,
            Instant createdAt,
            Instant updatedAt
    ) {
        Address address = new Address(
            id, customerId, recipientName, phoneNumber,
            postalCode, city, district, street, country, isDefault
        );
        address.createdAt = createdAt;
        address.updatedAt = updatedAt;
        return address;
    }

    /**
     * Updates the address details.
     */
    public void update(
            String recipientName,
            String phoneNumber,
            String postalCode,
            String city,
            String district,
            String street,
            String country
    ) {
        if (recipientName != null) this.recipientName = recipientName;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (postalCode != null) this.postalCode = postalCode;
        if (city != null) this.city = city;
        this.district = district;
        if (street != null) this.street = street;
        if (country != null) this.country = country;
        this.updatedAt = Instant.now();
    }

    public void setAsDefault(boolean isDefault) {
        this.isDefault = isDefault;
        this.updatedAt = Instant.now();
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(postalCode).append(" ");
        sb.append(city);
        if (district != null && !district.isBlank()) {
            sb.append(district);
        }
        sb.append(street);
        sb.append(", ").append(country);
        return sb.toString();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getCountry() {
        return country;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
