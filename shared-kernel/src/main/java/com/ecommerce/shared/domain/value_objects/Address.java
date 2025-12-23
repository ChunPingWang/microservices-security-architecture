package com.ecommerce.shared.domain.value_objects;

import java.util.Objects;

/**
 * Value object representing a physical address.
 * Immutable and thread-safe.
 */
public final class Address {

    private final String street;
    private final String city;
    private final String district;
    private final String postalCode;
    private final String country;
    private final String recipientName;
    private final String phoneNumber;

    private Address(Builder builder) {
        this.street = builder.street;
        this.city = builder.city;
        this.district = builder.district;
        this.postalCode = builder.postalCode;
        this.country = builder.country;
        this.recipientName = builder.recipientName;
        this.phoneNumber = builder.phoneNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullAddress() {
        return String.format(
            "%s %s%s%s, %s",
            postalCode,
            city,
            district,
            street,
            country
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(district, address.district) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, district, postalCode, country);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }

    /**
     * Builder for Address value object.
     */
    public static final class Builder {
        private String street;
        private String city;
        private String district;
        private String postalCode;
        private String country = "Taiwan";
        private String recipientName;
        private String phoneNumber;

        private Builder() {}

        public Builder street(String street) {
            this.street = Objects.requireNonNull(street);
            return this;
        }

        public Builder city(String city) {
            this.city = Objects.requireNonNull(city);
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = Objects.requireNonNull(postalCode);
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder recipientName(String recipientName) {
            this.recipientName = Objects.requireNonNull(recipientName);
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = Objects.requireNonNull(phoneNumber);
            return this;
        }

        public Address build() {
            validate();
            return new Address(this);
        }

        private void validate() {
            Objects.requireNonNull(street, "Street is required");
            Objects.requireNonNull(city, "City is required");
            Objects.requireNonNull(postalCode, "Postal code is required");
            Objects.requireNonNull(recipientName, "Recipient name is required");
            Objects.requireNonNull(phoneNumber, "Phone number is required");
        }
    }
}
