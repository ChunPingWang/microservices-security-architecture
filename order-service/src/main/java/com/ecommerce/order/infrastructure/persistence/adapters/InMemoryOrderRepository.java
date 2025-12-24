package com.ecommerce.order.infrastructure.persistence.adapters;

import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.ports.OrderRepository;
import com.ecommerce.order.domain.value_objects.OrderStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of OrderRepository.
 * For development and testing purposes only.
 */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .filter(order -> order.getStatus() == status)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Order> findPendingPaymentOrdersOlderThan(long timeoutMinutes) {
        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        return orders.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING_PAYMENT)
                .filter(order -> order.getCreatedAt().isBefore(cutoff))
                .toList();
    }

    @Override
    public boolean existsById(UUID orderId) {
        return orders.containsKey(orderId);
    }

    /**
     * Clears all orders. For testing purposes only.
     */
    public void clear() {
        orders.clear();
    }
}
