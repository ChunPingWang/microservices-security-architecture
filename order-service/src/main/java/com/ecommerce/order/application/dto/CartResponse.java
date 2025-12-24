package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.aggregates.Cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for shopping cart.
 */
public record CartResponse(
        UUID id,
        UUID customerId,
        List<CartItemResponse> items,
        int itemCount,
        int totalQuantity,
        BigDecimal total,
        String currency,
        Instant updatedAt
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getCustomerId(),
                itemResponses,
                cart.getItemCount(),
                cart.getTotalItemCount(),
                cart.getTotal().getAmount(),
                cart.getTotal().getCurrency().getCurrencyCode(),
                cart.getUpdatedAt()
        );
    }

    public static CartResponse empty(UUID customerId) {
        return new CartResponse(
                null,
                customerId,
                List.of(),
                0,
                0,
                BigDecimal.ZERO,
                "TWD",
                Instant.now()
        );
    }
}
