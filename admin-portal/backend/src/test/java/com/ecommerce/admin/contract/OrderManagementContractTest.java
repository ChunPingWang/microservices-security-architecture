package com.ecommerce.admin.contract;

import com.ecommerce.admin.application.dto.OrderSummary;
import com.ecommerce.admin.application.usecases.OrderManagementUseCase;
import com.ecommerce.admin.infrastructure.web.controllers.OrderManagementController;
import com.ecommerce.admin.infrastructure.web.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Order Management endpoints.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {OrderManagementController.class, GlobalExceptionHandler.class})
@DisplayName("Order Management Contract Tests")
class OrderManagementContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderManagementUseCase orderManagementUseCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("List Orders")
    class ListOrders {

        @Test
        @DisplayName("should return list of orders")
        void shouldReturnListOfOrders() throws Exception {
            List<OrderSummary> orders = List.of(
                    new OrderSummary(UUID.randomUUID(), UUID.randomUUID(), "PENDING",
                            new BigDecimal("35900"), LocalDateTime.now()),
                    new OrderSummary(UUID.randomUUID(), UUID.randomUUID(), "PAID",
                            new BigDecimal("59900"), LocalDateTime.now())
            );
            when(orderManagementUseCase.listOrders(any())).thenReturn(orders);

            mockMvc.perform(get("/api/admin/orders")
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].orderId").exists())
                    .andExpect(jsonPath("$[0].customerId").exists())
                    .andExpect(jsonPath("$[0].status").value("PENDING"))
                    .andExpect(jsonPath("$[0].totalAmount").value(35900));
        }

        @Test
        @DisplayName("should filter orders by status")
        void shouldFilterOrdersByStatus() throws Exception {
            List<OrderSummary> orders = List.of(
                    new OrderSummary(UUID.randomUUID(), UUID.randomUUID(), "PAID",
                            new BigDecimal("35900"), LocalDateTime.now())
            );
            when(orderManagementUseCase.listOrdersByStatus(any(), eq("PAID"))).thenReturn(orders);

            mockMvc.perform(get("/api/admin/orders")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .param("status", "PAID"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PAID"));
        }
    }

    @Nested
    @DisplayName("Get Order Detail")
    class GetOrderDetail {

        @Test
        @DisplayName("should return order details")
        void shouldReturnOrderDetails() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderSummary order = new OrderSummary(orderId, UUID.randomUUID(), "PAID",
                    new BigDecimal("35900"), LocalDateTime.now());
            when(orderManagementUseCase.getOrder(any(), eq(orderId))).thenReturn(order);

            mockMvc.perform(get("/api/admin/orders/{orderId}", orderId)
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                    .andExpect(jsonPath("$.status").value("PAID"));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            UUID orderId = UUID.randomUUID();
            when(orderManagementUseCase.getOrder(any(), eq(orderId))).thenReturn(null);

            mockMvc.perform(get("/api/admin/orders/{orderId}", orderId)
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update Order Status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status")
        void shouldUpdateOrderStatus() throws Exception {
            UUID orderId = UUID.randomUUID();
            when(orderManagementUseCase.updateOrderStatus(any(), eq(orderId), eq("SHIPPED"))).thenReturn(true);

            mockMvc.perform(patch("/api/admin/orders/{orderId}/status", orderId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "status": "SHIPPED"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            UUID orderId = UUID.randomUUID();
            when(orderManagementUseCase.updateOrderStatus(any(), eq(orderId), any())).thenReturn(false);

            mockMvc.perform(patch("/api/admin/orders/{orderId}/status", orderId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "status": "SHIPPED"
                                }
                                """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Cancel Order")
    class CancelOrder {

        @Test
        @DisplayName("should cancel order")
        void shouldCancelOrder() throws Exception {
            UUID orderId = UUID.randomUUID();
            when(orderManagementUseCase.cancelOrder(any(), eq(orderId), any())).thenReturn(true);

            mockMvc.perform(post("/api/admin/orders/{orderId}/cancel", orderId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "reason": "Customer requested cancellation"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
