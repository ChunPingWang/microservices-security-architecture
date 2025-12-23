package com.ecommerce.customer.unit.domain;

import com.ecommerce.customer.domain.value_objects.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Email value object.
 */
@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create email with valid address")
        void shouldCreateEmailWithValidAddress() {
            Email email = Email.of("user@example.com");

            assertThat(email).isNotNull();
            assertThat(email.getValue()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should normalize email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            Email email = Email.of("User@EXAMPLE.COM");

            assertThat(email.getValue()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should trim whitespace from email")
        void shouldTrimWhitespaceFromEmail() {
            Email email = Email.of("  user@example.com  ");

            assertThat(email.getValue()).isEqualTo("user@example.com");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should reject null or empty email")
        void shouldRejectNullOrEmptyEmail(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@example.com",
            "user@",
            "user@.com",
            "user@example.",
            "user@@example.com"
        })
        @DisplayName("should reject invalid email formats")
        void shouldRejectInvalidEmailFormats(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid email");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user@sub.example.com",
            "user123@example.co.tw"
        })
        @DisplayName("should accept valid email formats")
        void shouldAcceptValidEmailFormats(String validEmail) {
            Email email = Email.of(validEmail);
            assertThat(email).isNotNull();
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same email address")
        void shouldBeEqualForSameEmailAddress() {
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("user@example.com");

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("should be equal regardless of case")
        void shouldBeEqualRegardlessOfCase() {
            Email email1 = Email.of("USER@example.com");
            Email email2 = Email.of("user@EXAMPLE.com");

            assertThat(email1).isEqualTo(email2);
        }

        @Test
        @DisplayName("should not be equal for different email addresses")
        void shouldNotBeEqualForDifferentEmailAddresses() {
            Email email1 = Email.of("user1@example.com");
            Email email2 = Email.of("user2@example.com");

            assertThat(email1).isNotEqualTo(email2);
        }
    }
}
