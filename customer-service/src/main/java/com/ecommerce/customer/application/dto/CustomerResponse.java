package com.ecommerce.customer.application.dto;

import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.value_objects.MemberLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for customer data.
 */
public record CustomerResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String phoneNumber,
        MemberLevel memberLevel,
        BigDecimal totalSpending,
        boolean emailVerified,
        Instant createdAt
) {
    /**
     * Creates a CustomerResponse from a Customer entity.
     *
     * @param customer the customer entity
     * @return the response DTO
     */
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail().getValue(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getMemberLevel(),
                customer.getTotalSpending(),
                customer.isEmailVerified(),
                customer.getCreatedAt()
        );
    }
}
