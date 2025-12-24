package com.ecommerce.admin.infrastructure.web.controllers;

import com.ecommerce.admin.application.dto.CustomerSummary;
import com.ecommerce.admin.application.usecases.CustomerManagementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for customer management.
 */
@RestController
@RequestMapping("/api/admin/customers")
public class CustomerManagementController {

    private final CustomerManagementUseCase customerManagementUseCase;

    public CustomerManagementController(CustomerManagementUseCase customerManagementUseCase) {
        this.customerManagementUseCase = customerManagementUseCase;
    }

    /**
     * Lists all customers with optional member level filter.
     * GET /api/admin/customers
     * GET /api/admin/customers?memberLevel=GOLD
     */
    @GetMapping
    public ResponseEntity<List<CustomerSummary>> listCustomers(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam(required = false) String memberLevel) {

        List<CustomerSummary> customers;
        if (memberLevel != null && !memberLevel.isBlank()) {
            customers = customerManagementUseCase.listCustomersByMemberLevel(adminId, memberLevel);
        } else {
            customers = customerManagementUseCase.listCustomers(adminId);
        }
        return ResponseEntity.ok(customers);
    }

    /**
     * Gets customer details.
     * GET /api/admin/customers/{customerId}
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerSummary> getCustomer(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID customerId) {

        CustomerSummary customer = customerManagementUseCase.getCustomer(adminId, customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    /**
     * Toggles customer account status.
     * PATCH /api/admin/customers/{customerId}/status
     */
    @PatchMapping("/{customerId}/status")
    public ResponseEntity<Map<String, Object>> toggleCustomerStatus(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID customerId,
            @RequestBody Map<String, Boolean> request) {

        Boolean active = request.get("active");
        boolean success = customerManagementUseCase.toggleCustomerStatus(adminId, customerId, active);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Updates customer member level.
     * PATCH /api/admin/customers/{customerId}/member-level
     */
    @PatchMapping("/{customerId}/member-level")
    public ResponseEntity<Map<String, Object>> updateMemberLevel(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID customerId,
            @RequestBody Map<String, String> request) {

        String memberLevel = request.get("memberLevel");
        boolean success = customerManagementUseCase.updateMemberLevel(adminId, customerId, memberLevel);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
