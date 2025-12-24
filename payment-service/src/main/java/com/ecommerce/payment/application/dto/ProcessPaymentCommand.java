package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.value_objects.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to process a payment for an order.
 */
public record ProcessPaymentCommand(
        @NotNull(message = "Order ID is required")
        UUID orderId,
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,
        PaymentDetails paymentDetails
) {
    public record PaymentDetails(
            String cardNumber,
            String cardHolderName,
            String expiryMonth,
            String expiryYear,
            String cvv
    ) {}
}
