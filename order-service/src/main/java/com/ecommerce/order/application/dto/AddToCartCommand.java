package com.ecommerce.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command DTO for adding item to cart.
 */
public record AddToCartCommand(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
    public AddToCartCommand {
        if (quantity < 1) {
            quantity = 1;
        }
    }
}
