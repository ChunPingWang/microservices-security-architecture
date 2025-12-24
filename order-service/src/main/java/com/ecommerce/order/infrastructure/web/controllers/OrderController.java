package com.ecommerce.order.infrastructure.web.controllers;

import com.ecommerce.order.application.dto.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.usecases.CreateOrderUseCase;
import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.ports.OrderRepository;
import com.ecommerce.order.application.exceptions.OrderNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for order operations.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderRepository orderRepository;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           OrderRepository orderRepository) {
        this.createOrderUseCase = createOrderUseCase;
        this.orderRepository = orderRepository;
    }

    /**
     * Create a new order from the customer's cart.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Principal principal,
            @Valid @RequestBody CreateOrderCommand command
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        OrderResponse response = createOrderUseCase.execute(customerId, command);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order by ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            Principal principal,
            @PathVariable UUID orderId
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Ensure customer owns this order
        if (!order.getCustomerId().equals(customerId)) {
            throw new OrderNotFoundException(orderId);
        }

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * Get all orders for the current customer.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(Principal principal) {
        UUID customerId = UUID.fromString(principal.getName());
        List<OrderResponse> orders = orderRepository.findByCustomerId(customerId)
                .stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }
}
