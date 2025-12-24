package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.entities.CartItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for cart item.
 */
public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSku,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal subtotal,
        Instant addedAt
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductSku(),
                item.getUnitPrice().getAmount(),
                item.getUnitPrice().getCurrency().getCurrencyCode(),
                item.getQuantityValue(),
                item.getSubtotal().getAmount(),
                item.getAddedAt()
        );
    }
}
