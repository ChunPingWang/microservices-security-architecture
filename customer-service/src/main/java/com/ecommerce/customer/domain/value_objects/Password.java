package com.ecommerce.customer.domain.value_objects;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a password.
 * Stores BCrypt hash, never the raw password.
 */
public final class Password {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    // At least 8 chars, one uppercase, one lowercase, one digit
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    private final String hash;

    private Password(String hash) {
        this.hash = hash;
    }

    /**
     * Creates a Password from a raw password string.
     * Validates strength and hashes using BCrypt.
     *
     * @param rawPassword the raw password
     * @return a Password with hashed value
     * @throws IllegalArgumentException if password is weak
     */
    public static Password fromRaw(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters and contain " +
                "uppercase, lowercase, and digit"
            );
        }

        String hash = ENCODER.encode(rawPassword);
        return new Password(hash);
    }

    /**
     * Creates a Password from an existing hash.
     * Used when loading from database.
     *
     * @param hash the BCrypt hash
     * @return a Password instance
     */
    public static Password fromHash(String hash) {
        Objects.requireNonNull(hash, "Hash must not be null");
        return new Password(hash);
    }

    /**
     * Verifies if the given raw password matches this hash.
     *
     * @param rawPassword the password to verify
     * @return true if matches
     */
    public boolean matches(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, hash);
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return hash.equals(password.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
