package com.ecommerce.payment.infrastructure.clients;

import com.ecommerce.payment.domain.ports.OrderServicePort;
import com.ecommerce.shared.domain.value_objects.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock order service adapter for development and testing.
 * In production, this would be replaced with a Feign client.
 */
@Component
public class MockOrderServiceAdapter implements OrderServicePort {

    private static final Logger log = LoggerFactory.getLogger(MockOrderServiceAdapter.class);

    // Mock order store for testing
    private final Map<UUID, OrderInfo> orders = new ConcurrentHashMap<>();

    @Override
    public Optional<OrderInfo> getOrderInfo(UUID orderId) {
        log.debug("Getting order info for: {}", orderId);

        // Return mock order if exists
        if (orders.containsKey(orderId)) {
            return Optional.of(orders.get(orderId));
        }

        // Create a mock order for testing
        OrderInfo mockOrder = new OrderInfo(
                orderId,
                UUID.randomUUID(),
                Money.of(new BigDecimal("999.00")),
                "PENDING_PAYMENT"
        );
        orders.put(orderId, mockOrder);

        return Optional.of(mockOrder);
    }

    @Override
    public void notifyPaymentComplete(UUID orderId, UUID paymentId) {
        log.info("Notifying order service: payment complete for order {} with payment {}",
                orderId, paymentId);
        // In real implementation, this would call the order service
    }

    @Override
    public void notifyPaymentFailed(UUID orderId, String reason) {
        log.info("Notifying order service: payment failed for order {} - {}",
                orderId, reason);
        // In real implementation, this would call the order service
    }

    /**
     * Add a mock order for testing.
     */
    public void addOrder(UUID orderId, UUID customerId, Money amount) {
        orders.put(orderId, new OrderInfo(orderId, customerId, amount, "PENDING_PAYMENT"));
    }

    /**
     * Clear all mock orders.
     */
    public void clear() {
        orders.clear();
    }
}
