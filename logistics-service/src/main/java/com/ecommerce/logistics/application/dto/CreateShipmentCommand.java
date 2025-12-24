package com.ecommerce.logistics.application.dto;

import com.ecommerce.logistics.domain.value_objects.Carrier;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command for creating a new shipment.
 */
public record CreateShipmentCommand(
        @NotNull UUID orderId,
        @NotNull UUID customerId,
        @NotNull ShippingAddressDto shippingAddress,
        @NotNull Carrier carrier
) {
    public record ShippingAddressDto(
            String street,
            String city,
            String district,
            String postalCode,
            String country,
            String recipientName,
            String phoneNumber
    ) {}
}
