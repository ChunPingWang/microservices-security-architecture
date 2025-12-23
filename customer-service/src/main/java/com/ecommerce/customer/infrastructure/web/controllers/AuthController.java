package com.ecommerce.customer.infrastructure.web.controllers;

import com.ecommerce.customer.application.dto.AuthenticateCommand;
import com.ecommerce.customer.application.dto.AuthenticationResponse;
import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.ecommerce.customer.application.usecases.AuthenticateCustomerUseCase;
import com.ecommerce.customer.application.usecases.RegisterCustomerUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final AuthenticateCustomerUseCase authenticateCustomerUseCase;

    public AuthController(
            RegisterCustomerUseCase registerCustomerUseCase,
            AuthenticateCustomerUseCase authenticateCustomerUseCase
    ) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.authenticateCustomerUseCase = authenticateCustomerUseCase;
    }

    /**
     * Registers a new customer.
     *
     * @param command the registration command
     * @return the created customer
     */
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(
            @Valid @RequestBody RegisterCustomerCommand command
    ) {
        CustomerResponse response = registerCustomerUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a customer.
     *
     * @param command the authentication command
     * @return the authentication response with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticateCommand command
    ) {
        AuthenticationResponse response = authenticateCustomerUseCase.execute(command);
        return ResponseEntity.ok(response);
    }
}
