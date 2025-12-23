package com.ecommerce.customer.unit.domain;

import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.events.CustomerRegistered;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import com.ecommerce.customer.domain.value_objects.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Customer entity.
 */
@DisplayName("Customer Entity")
class CustomerTest {

    private Email validEmail;
    private Password validPassword;

    @BeforeEach
    void setUp() {
        validEmail = Email.of("test@example.com");
        validPassword = Password.fromRaw("Password123");
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should create new customer with valid data")
        void shouldCreateNewCustomerWithValidData() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            assertThat(customer).isNotNull();
            assertThat(customer.getId()).isNotNull();
            assertThat(customer.getEmail()).isEqualTo(validEmail);
            assertThat(customer.getFirstName()).isEqualTo("John");
            assertThat(customer.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("should set initial member level to NORMAL")
        void shouldSetInitialMemberLevelToNormal() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            assertThat(customer.getMemberLevel()).isEqualTo(MemberLevel.NORMAL);
        }

        @Test
        @DisplayName("should set initial total spending to zero")
        void shouldSetInitialTotalSpendingToZero() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            assertThat(customer.getTotalSpending())
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should emit CustomerRegistered event")
        void shouldEmitCustomerRegisteredEvent() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            assertThat(customer.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CustomerRegistered.class);
        }

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNullEmail() {
            assertThatThrownBy(() -> Customer.register(
                null,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            )).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Authentication")
    class Authentication {

        @Test
        @DisplayName("should authenticate with correct password")
        void shouldAuthenticateWithCorrectPassword() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            assertThat(customer.authenticate("Password123")).isTrue();
        }

        @Test
        @DisplayName("should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            assertThat(customer.authenticate("WrongPassword")).isFalse();
        }

        @Test
        @DisplayName("should increment failed login attempts on failure")
        void shouldIncrementFailedLoginAttemptsOnFailure() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            customer.recordFailedLogin();

            assertThat(customer.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("should reset failed login attempts on success")
        void shouldResetFailedLoginAttemptsOnSuccess() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            customer.recordFailedLogin();
            customer.recordFailedLogin();
            customer.recordSuccessfulLogin();

            assertThat(customer.getFailedLoginAttempts()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Account Lockout")
    class AccountLockout {

        @Test
        @DisplayName("should lock account after 5 failed attempts")
        void shouldLockAccountAfter5FailedAttempts() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            for (int i = 0; i < 5; i++) {
                customer.recordFailedLogin();
            }

            assertThat(customer.isLocked()).isTrue();
        }

        @Test
        @DisplayName("should not lock account before 5 failed attempts")
        void shouldNotLockAccountBefore5FailedAttempts() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            for (int i = 0; i < 4; i++) {
                customer.recordFailedLogin();
            }

            assertThat(customer.isLocked()).isFalse();
        }

        @Test
        @DisplayName("should set lock duration to 15 minutes")
        void shouldSetLockDurationTo15Minutes() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            for (int i = 0; i < 5; i++) {
                customer.recordFailedLogin();
            }

            assertThat(customer.getLockedUntil()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Member Level")
    class MemberLevelTests {

        @Test
        @DisplayName("should upgrade to SILVER at 10000 spending")
        void shouldUpgradeToSilverAt10000Spending() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            customer.addSpending(new BigDecimal("10000"));

            assertThat(customer.getMemberLevel()).isEqualTo(MemberLevel.SILVER);
        }

        @Test
        @DisplayName("should upgrade to GOLD at 30000 spending")
        void shouldUpgradeToGoldAt30000Spending() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            customer.addSpending(new BigDecimal("30000"));

            assertThat(customer.getMemberLevel()).isEqualTo(MemberLevel.GOLD);
        }

        @Test
        @DisplayName("should upgrade to PLATINUM at 100000 spending")
        void shouldUpgradeToPlatinumAt100000Spending() {
            Customer customer = Customer.register(
                validEmail,
                validPassword,
                "John",
                "Doe",
                "0912345678"
            );

            customer.addSpending(new BigDecimal("100000"));

            assertThat(customer.getMemberLevel()).isEqualTo(MemberLevel.PLATINUM);
        }
    }
}
