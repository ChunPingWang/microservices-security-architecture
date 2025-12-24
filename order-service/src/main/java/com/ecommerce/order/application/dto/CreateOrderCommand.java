package com.ecommerce.order.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Command to create an order from the current cart.
 */
public record CreateOrderCommand(
        @NotNull(message = "Shipping address is required")
        ShippingAddress shippingAddress,
        String couponCode
) {
    public record ShippingAddress(
            @NotNull(message = "Recipient name is required")
            String recipientName,
            @NotNull(message = "Phone number is required")
            String phone,
            @NotNull(message = "Address line 1 is required")
            String addressLine1,
            String addressLine2,
            @NotNull(message = "City is required")
            String city,
            String state,
            @NotNull(message = "Postal code is required")
            String postalCode,
            @NotNull(message = "Country is required")
            String country
    ) {}
}
