package com.ecommerce.customer.unit.domain;

import com.ecommerce.customer.domain.value_objects.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Password value object.
 */
@DisplayName("Password Value Object")
class PasswordTest {

    @Nested
    @DisplayName("Creation from raw password")
    class CreationFromRawPassword {

        @Test
        @DisplayName("should create password with valid input")
        void shouldCreatePasswordWithValidInput() {
            Password password = Password.fromRaw("Password123");

            assertThat(password).isNotNull();
            assertThat(password.getHash()).isNotBlank();
        }

        @Test
        @DisplayName("should hash password using BCrypt")
        void shouldHashPasswordUsingBCrypt() {
            Password password = Password.fromRaw("Password123");

            assertThat(password.getHash()).startsWith("$2");
        }

        @Test
        @DisplayName("should create different hashes for same password")
        void shouldCreateDifferentHashesForSamePassword() {
            Password password1 = Password.fromRaw("Password123");
            Password password2 = Password.fromRaw("Password123");

            assertThat(password1.getHash()).isNotEqualTo(password2.getHash());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should reject null or empty password")
        void shouldRejectNullOrEmptyPassword(String invalidPassword) {
            assertThatThrownBy(() -> Password.fromRaw(invalidPassword))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "pass",       // Too short
            "password",   // No uppercase or digit
            "PASSWORD",   // No lowercase or digit
            "Password",   // No digit
            "password1",  // No uppercase
            "PASSWORD1",  // No lowercase
            "Pass1"       // Too short
        })
        @DisplayName("should reject weak passwords")
        void shouldRejectWeakPasswords(String weakPassword) {
            assertThatThrownBy(() -> Password.fromRaw(weakPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Password1",
            "MyPass123",
            "Secure@Password1",
            "C0mplexP@ss"
        })
        @DisplayName("should accept strong passwords")
        void shouldAcceptStrongPasswords(String strongPassword) {
            Password password = Password.fromRaw(strongPassword);
            assertThat(password).isNotNull();
        }
    }

    @Nested
    @DisplayName("Verification")
    class Verification {

        @Test
        @DisplayName("should verify correct password")
        void shouldVerifyCorrectPassword() {
            Password password = Password.fromRaw("Password123");

            assertThat(password.matches("Password123")).isTrue();
        }

        @Test
        @DisplayName("should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            Password password = Password.fromRaw("Password123");

            assertThat(password.matches("WrongPassword")).isFalse();
        }

        @Test
        @DisplayName("should reject null password for verification")
        void shouldRejectNullPasswordForVerification() {
            Password password = Password.fromRaw("Password123");

            assertThat(password.matches(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("From hash")
    class FromHash {

        @Test
        @DisplayName("should create password from existing hash")
        void shouldCreatePasswordFromExistingHash() {
            Password original = Password.fromRaw("Password123");
            String hash = original.getHash();

            Password restored = Password.fromHash(hash);

            assertThat(restored.getHash()).isEqualTo(hash);
            assertThat(restored.matches("Password123")).isTrue();
        }
    }
}
