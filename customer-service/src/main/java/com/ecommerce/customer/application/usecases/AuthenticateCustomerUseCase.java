package com.ecommerce.customer.application.usecases;

import com.ecommerce.customer.application.dto.AuthenticateCommand;
import com.ecommerce.customer.application.dto.AuthenticationResponse;
import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.exceptions.AuthenticationFailedException;
import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.security.provider.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for authenticating a customer.
 */
@Service
public class AuthenticateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticateCustomerUseCase(
            CustomerRepository customerRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.customerRepository = customerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticates a customer and returns tokens.
     *
     * @param command the authentication command
     * @return authentication response with tokens
     * @throws AuthenticationFailedException if authentication fails
     */
    @Transactional
    public AuthenticationResponse execute(AuthenticateCommand command) {
        Email email = Email.of(command.email());

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(AuthenticationFailedException::invalidCredentials);

        if (customer.isLocked()) {
            throw AuthenticationFailedException.accountLocked();
        }

        if (!customer.authenticate(command.password())) {
            customer.recordFailedLogin();
            customerRepository.save(customer);
            throw AuthenticationFailedException.invalidCredentials();
        }

        customer.recordSuccessfulLogin();
        customerRepository.save(customer);

        String accessToken = jwtTokenProvider.generateAccessToken(
                customer.getId().toString(),
                customer.getEmail().getValue(),
                "ROLE_CUSTOMER"
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                customer.getId().toString()
        );

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                CustomerResponse.from(customer)
        );
    }
}
