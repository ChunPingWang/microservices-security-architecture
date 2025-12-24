package com.ecommerce.product.domain.value_objects;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Stock Keeping Unit (SKU).
 * SKUs are unique product identifiers.
 */
public final class SKU {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9-]+$");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final String value;

    private SKU(String value) {
        this.value = value;
    }

    /**
     * Creates a SKU from a string value.
     *
     * @param value the SKU string
     * @return a SKU instance
     * @throws IllegalArgumentException if value is invalid
     */
    public static SKU of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU must not be null or empty");
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "SKU must be at least " + MIN_LENGTH + " characters"
            );
        }

        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "SKU must not exceed " + MAX_LENGTH + " characters"
            );
        }

        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "SKU must contain only alphanumeric characters and hyphens"
            );
        }

        return new SKU(normalized);
    }

    /**
     * Generates a random SKU with a given prefix.
     *
     * @param prefix the prefix for the SKU
     * @return a generated SKU
     */
    public static SKU generate(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.isBlank()) {
            sb.append(prefix.toUpperCase().trim()).append("-");
        }

        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return new SKU(sb.toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SKU sku = (SKU) o;
        return value.equals(sku.value);
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
