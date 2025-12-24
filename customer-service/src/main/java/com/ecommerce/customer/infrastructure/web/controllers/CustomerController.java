package com.ecommerce.customer.infrastructure.web.controllers;

import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.dto.MembershipResponse;
import com.ecommerce.customer.application.usecases.GetCustomerProfileUseCase;
import com.ecommerce.customer.application.usecases.GetMembershipUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for customer profile endpoints.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final GetCustomerProfileUseCase getCustomerProfileUseCase;
    private final GetMembershipUseCase getMembershipUseCase;

    public CustomerController(
            GetCustomerProfileUseCase getCustomerProfileUseCase,
            GetMembershipUseCase getMembershipUseCase
    ) {
        this.getCustomerProfileUseCase = getCustomerProfileUseCase;
        this.getMembershipUseCase = getMembershipUseCase;
    }

    /**
     * Gets the current customer's profile.
     * GET /api/customers/me
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId) {
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomerResponse response = getCustomerProfileUseCase.execute(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current customer's membership information.
     * GET /api/customers/me/membership
     */
    @GetMapping("/me/membership")
    public ResponseEntity<MembershipResponse> getMyMembership(
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId) {
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MembershipResponse response = getMembershipUseCase.getMembership(customerId);
        return ResponseEntity.ok(response);
    }
}
