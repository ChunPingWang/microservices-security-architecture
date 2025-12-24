package com.ecommerce.order.contract;

import com.ecommerce.order.application.usecases.CreateOrderUseCase;
import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.entities.OrderItem;
import com.ecommerce.order.domain.ports.CartRepository;
import com.ecommerce.order.domain.ports.OrderRepository;
import com.ecommerce.order.domain.value_objects.OrderStatus;
import com.ecommerce.order.infrastructure.web.GlobalExceptionHandler;
import com.ecommerce.order.infrastructure.web.controllers.OrderController;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/orders endpoint (order history).
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {OrderController.class, GlobalExceptionHandler.class, CreateOrderUseCase.class})
@DisplayName("Get Orders Contract Tests")
class GetOrdersContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private OrderRepository orderRepository;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Response Contract - Order List")
    class OrderListResponseContract {

        @Test
        @DisplayName("should return empty list when customer has no orders")
        void shouldReturnEmptyListWhenNoOrders() throws Exception {
            when(orderRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("should return list of orders with all required fields")
        void shouldReturnOrderListWithRequiredFields() throws Exception {
            Order order = createTestOrder();
            when(orderRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(order));

            mockMvc.perform(get("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$[0].status").exists())
                    .andExpect(jsonPath("$[0].total").isNumber())
                    .andExpect(jsonPath("$[0].currency").isString())
                    .andExpect(jsonPath("$[0].itemCount").isNumber())
                    .andExpect(jsonPath("$[0].createdAt").exists());
        }

        @Test
        @DisplayName("should return multiple orders sorted by date")
        void shouldReturnMultipleOrdersSortedByDate() throws Exception {
            Order order1 = createTestOrder();
            Order order2 = createTestOrder();
            when(orderRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(order1, order2));

            mockMvc.perform(get("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("should include order items in response")
        void shouldIncludeOrderItemsInResponse() throws Exception {
            Order order = createTestOrder();
            when(orderRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(order));

            mockMvc.perform(get("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].items").isArray())
                    .andExpect(jsonPath("$[0].items[0].productId").exists())
                    .andExpect(jsonPath("$[0].items[0].productName").exists())
                    .andExpect(jsonPath("$[0].items[0].quantity").isNumber());
        }
    }

    @Nested
    @DisplayName("Response Contract - Order Status Display")
    class OrderStatusDisplayContract {

        @Test
        @DisplayName("should return PENDING_PAYMENT status for new orders")
        void shouldReturnPendingPaymentStatus() throws Exception {
            Order order = createTestOrder();
            when(orderRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(order));

            mockMvc.perform(get("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("PENDING_PAYMENT"));
        }

        @Test
        @DisplayName("should include tracking number for shipped orders")
        void shouldIncludeTrackingNumberForShippedOrders() throws Exception {
            Order order = createShippedOrder();
            when(orderRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(order));

            mockMvc.perform(get("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("SHIPPED"))
                    .andExpect(jsonPath("$[0].trackingNumber").value("TRACK-12345"));
        }
    }

    @Nested
    @DisplayName("Response Contract - Get Single Order")
    class GetSingleOrderContract {

        @Test
        @DisplayName("should return order by ID with all fields")
        void shouldReturnOrderByIdWithAllFields() throws Exception {
            Order order = createTestOrder();
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            mockMvc.perform(get("/api/v1/orders/{orderId}", order.getId())
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(order.getId().toString()))
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.total").isNumber())
                    .andExpect(jsonPath("$.status").exists());
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
        }

        @Test
        @DisplayName("should return 404 when order belongs to different customer")
        void shouldReturn404WhenOrderBelongsToDifferentCustomer() throws Exception {
            UUID differentCustomerId = UUID.randomUUID();
            Order order = createOrderForCustomer(differentCustomerId);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            mockMvc.perform(get("/api/v1/orders/{orderId}", order.getId())
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    private Order createTestOrder() {
        return createOrderForCustomer(CUSTOMER_ID);
    }

    private Order createOrderForCustomer(UUID customerId) {
        OrderItem item = OrderItem.create(
                UUID.randomUUID(),
                "Test Product",
                "SKU-001",
                Money.of(new BigDecimal("999.00")),
                2
        );
        return Order.createFromCart(customerId, List.of(item), null);
    }

    private Order createShippedOrder() {
        Order order = createTestOrder();
        order.markAsPaid(UUID.randomUUID());
        order.markAsShipped("TRACK-12345");
        return order;
    }
}
