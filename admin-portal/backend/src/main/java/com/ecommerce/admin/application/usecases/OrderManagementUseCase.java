package com.ecommerce.admin.application.usecases;

import com.ecommerce.admin.application.dto.OrderSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Use case for order management operations.
 * Note: In a real implementation, this would call the order-service via Feign client.
 */
@Service
public class OrderManagementUseCase {

    // Mock data store for demo purposes
    private final Map<UUID, OrderSummary> orders = new ConcurrentHashMap<>();

    public OrderManagementUseCase() {
        // Initialize with some mock data
        UUID order1 = UUID.randomUUID();
        UUID order2 = UUID.randomUUID();
        UUID order3 = UUID.randomUUID();
        UUID customer1 = UUID.randomUUID();
        UUID customer2 = UUID.randomUUID();

        orders.put(order1, new OrderSummary(order1, customer1, "PENDING",
                new BigDecimal("35900"), LocalDateTime.now().minusHours(2)));
        orders.put(order2, new OrderSummary(order2, customer1, "PAID",
                new BigDecimal("59900"), LocalDateTime.now().minusDays(1)));
        orders.put(order3, new OrderSummary(order3, customer2, "SHIPPED",
                new BigDecimal("12500"), LocalDateTime.now().minusDays(3)));
    }

    /**
     * Lists all orders.
     */
    public List<OrderSummary> listOrders(UUID adminId) {
        return new ArrayList<>(orders.values());
    }

    /**
     * Lists orders by status.
     */
    public List<OrderSummary> listOrdersByStatus(UUID adminId, String status) {
        return orders.values().stream()
                .filter(order -> order.status().equals(status))
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific order.
     */
    public OrderSummary getOrder(UUID adminId, UUID orderId) {
        return orders.get(orderId);
    }

    /**
     * Updates order status.
     */
    public boolean updateOrderStatus(UUID adminId, UUID orderId, String status) {
        OrderSummary existing = orders.get(orderId);
        if (existing == null) {
            return false;
        }

        OrderSummary updated = new OrderSummary(
                existing.orderId(),
                existing.customerId(),
                status,
                existing.totalAmount(),
                existing.createdAt()
        );
        orders.put(orderId, updated);
        return true;
    }

    /**
     * Cancels an order.
     */
    public boolean cancelOrder(UUID adminId, UUID orderId, String reason) {
        OrderSummary existing = orders.get(orderId);
        if (existing == null) {
            return false;
        }

        OrderSummary updated = new OrderSummary(
                existing.orderId(),
                existing.customerId(),
                "CANCELLED",
                existing.totalAmount(),
                existing.createdAt()
        );
        orders.put(orderId, updated);
        return true;
    }
}
