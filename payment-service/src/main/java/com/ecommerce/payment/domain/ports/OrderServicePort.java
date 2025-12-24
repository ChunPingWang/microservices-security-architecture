package com.ecommerce.payment.domain.ports;

import com.ecommerce.shared.domain.value_objects.Money;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for communicating with Order Service.
 */
public interface OrderServicePort {

    /**
     * Gets order information for payment.
     */
    Optional<OrderInfo> getOrderInfo(UUID orderId);

    /**
     * Notifies order service that payment is complete.
     */
    void notifyPaymentComplete(UUID orderId, UUID paymentId);

    /**
     * Notifies order service that payment failed.
     */
    void notifyPaymentFailed(UUID orderId, String reason);

    /**
     * Order information needed for payment.
     */
    record OrderInfo(
            UUID id,
            UUID customerId,
            Money totalAmount,
            String status
    ) {}
}
