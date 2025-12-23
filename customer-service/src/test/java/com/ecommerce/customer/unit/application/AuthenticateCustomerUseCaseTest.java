package com.ecommerce.customer.unit.application;

import com.ecommerce.customer.application.dto.AuthenticateCommand;
import com.ecommerce.customer.application.exceptions.AuthenticationFailedException;
import com.ecommerce.customer.application.usecases.AuthenticateCustomerUseCase;
import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.customer.domain.value_objects.Password;
import com.ecommerce.security.provider.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthenticateCustomerUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateCustomerUseCase Tests")
class AuthenticateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateCustomerUseCase(customerRepository, jwtTokenProvider);
    }

    @Nested
    @DisplayName("Account Lockout")
    class AccountLockout {

        @Test
        @DisplayName("should lock account after 5 failed attempts")
        void shouldLockAccountAfterFiveFailedAttempts() {
            String email = "test@example.com";
            String correctPassword = "Password123";

            // Create customer and keep reference to track state
            Customer customer = Customer.register(
                    Email.of(email),
                    Password.fromRaw(correctPassword),
                    "Test",
                    "User",
                    null
            );
            customer.clearDomainEvents();

            // Use AtomicReference to track customer state across mock calls
            AtomicReference<Customer> customerRef = new AtomicReference<>(customer);

            when(customerRepository.findByEmail(any(Email.class)))
                    .thenAnswer(inv -> Optional.of(customerRef.get()));
            when(customerRepository.save(any(Customer.class)))
                    .thenAnswer(inv -> {
                        Customer saved = inv.getArgument(0);
                        customerRef.set(saved);
                        return saved;
                    });

            AuthenticateCommand wrongPasswordCommand = new AuthenticateCommand(
                    email,
                    "WrongPass1"
            );

            // First 5 attempts should throw InvalidCredentials
            for (int i = 1; i <= 5; i++) {
                final int attempt = i;
                assertThatThrownBy(() -> useCase.execute(wrongPasswordCommand))
                        .isInstanceOf(AuthenticationFailedException.class)
                        .satisfies(ex -> {
                            AuthenticationFailedException authEx = (AuthenticationFailedException) ex;
                            assertThat(authEx.isAccountLocked()).isFalse();
                        });
            }

            // After 5 failed attempts, account should be locked
            assertThat(customerRef.get().isLocked()).isTrue();

            // 6th attempt should throw AccountLocked
            assertThatThrownBy(() -> useCase.execute(wrongPasswordCommand))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .satisfies(ex -> {
                        AuthenticationFailedException authEx = (AuthenticationFailedException) ex;
                        assertThat(authEx.isAccountLocked()).isTrue();
                    });
        }
    }
}
