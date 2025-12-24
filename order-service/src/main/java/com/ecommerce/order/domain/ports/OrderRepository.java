package com.ecommerce.order.domain.ports;

import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.value_objects.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Order aggregate.
 */
public interface OrderRepository {

    /**
     * Saves an order.
     */
    Order save(Order order);

    /**
     * Finds an order by ID.
     */
    Optional<Order> findById(UUID orderId);

    /**
     * Finds orders by customer ID.
     */
    List<Order> findByCustomerId(UUID customerId);

    /**
     * Finds orders by customer ID and status.
     */
    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);

    /**
     * Finds orders with pending payment that have exceeded the timeout.
     */
    List<Order> findPendingPaymentOrdersOlderThan(long timeoutMinutes);

    /**
     * Checks if an order exists.
     */
    boolean existsById(UUID orderId);
}
