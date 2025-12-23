package com.ecommerce.customer.domain.value_objects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a validated email address.
 * Immutable and always in normalized lowercase form.
 */
public final class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final String value;

    private Email(String value) {
        this.value = value.toLowerCase().trim();
    }

    /**
     * Creates an Email from a string value.
     *
     * @param value the email address string
     * @return a validated Email instance
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }

        String trimmed = value.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Must be a valid email address: " + trimmed
            );
        }

        return new Email(trimmed);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
