package com.ecommerce.sales.domain.value_objects;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * Value object representing a coupon code.
 */
public final class CouponCode {

    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String value;

    private CouponCode(String value) {
        this.value = Objects.requireNonNull(value, "Coupon code value must not be null")
                .toUpperCase()
                .trim();

        if (this.value.isEmpty()) {
            throw new IllegalArgumentException("Coupon code cannot be empty");
        }

        if (this.value.length() < 4 || this.value.length() > 20) {
            throw new IllegalArgumentException("Coupon code must be between 4 and 20 characters");
        }

        if (!this.value.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Coupon code can only contain letters and numbers");
        }
    }

    /**
     * Creates a CouponCode from an existing value.
     */
    public static CouponCode of(String value) {
        return new CouponCode(value);
    }

    /**
     * Generates a random coupon code.
     */
    public static CouponCode generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generates a random coupon code with specified length.
     */
    public static CouponCode generate(int length) {
        if (length < 4 || length > 20) {
            throw new IllegalArgumentException("Code length must be between 4 and 20");
        }

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALLOWED_CHARS.charAt(RANDOM.nextInt(ALLOWED_CHARS.length())));
        }
        return new CouponCode(code.toString());
    }

    /**
     * Generates a coupon code with a prefix.
     */
    public static CouponCode generateWithPrefix(String prefix) {
        String random = generate(4).value;
        return new CouponCode(prefix.toUpperCase() + random);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponCode that = (CouponCode) o;
        return value.equals(that.value);
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
