package com.ecommerce.customer.infrastructure.web.controllers;

import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.usecases.GetCustomerProfileUseCase;
import com.ecommerce.security.context.CurrentUserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for customer profile endpoints.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final GetCustomerProfileUseCase getCustomerProfileUseCase;
    private final CurrentUserContext currentUserContext;

    public CustomerController(
            GetCustomerProfileUseCase getCustomerProfileUseCase,
            CurrentUserContext currentUserContext
    ) {
        this.getCustomerProfileUseCase = getCustomerProfileUseCase;
        this.currentUserContext = currentUserContext;
    }

    /**
     * Gets the current customer's profile.
     *
     * @return the customer profile
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile() {
        UUID customerId = UUID.fromString(currentUserContext.requireUserId());
        CustomerResponse response = getCustomerProfileUseCase.execute(customerId);
        return ResponseEntity.ok(response);
    }
}
