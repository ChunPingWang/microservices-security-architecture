package com.ecommerce.admin.infrastructure.web.controllers;

import com.ecommerce.admin.application.dto.OrderSummary;
import com.ecommerce.admin.application.usecases.OrderManagementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for order management.
 */
@RestController
@RequestMapping("/api/admin/orders")
public class OrderManagementController {

    private final OrderManagementUseCase orderManagementUseCase;

    public OrderManagementController(OrderManagementUseCase orderManagementUseCase) {
        this.orderManagementUseCase = orderManagementUseCase;
    }

    /**
     * Lists all orders with optional status filter.
     * GET /api/admin/orders
     * GET /api/admin/orders?status=PAID
     */
    @GetMapping
    public ResponseEntity<List<OrderSummary>> listOrders(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam(required = false) String status) {

        List<OrderSummary> orders;
        if (status != null && !status.isBlank()) {
            orders = orderManagementUseCase.listOrdersByStatus(adminId, status);
        } else {
            orders = orderManagementUseCase.listOrders(adminId);
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * Gets order details.
     * GET /api/admin/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderSummary> getOrder(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID orderId) {

        OrderSummary order = orderManagementUseCase.getOrder(adminId, orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * Updates order status.
     * PATCH /api/admin/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        boolean success = orderManagementUseCase.updateOrderStatus(adminId, orderId, status);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Cancels an order.
     * POST /api/admin/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> request) {

        String reason = request.get("reason");
        boolean success = orderManagementUseCase.cancelOrder(adminId, orderId, reason);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
