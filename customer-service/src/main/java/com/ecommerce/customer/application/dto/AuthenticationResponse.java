package com.ecommerce.customer.application.dto;

/**
 * Response DTO for successful authentication.
 */
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        CustomerResponse customer
) {
}
