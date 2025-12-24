package com.ecommerce.order.contract;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.usecases.CreateOrderUseCase;
import com.ecommerce.order.domain.aggregates.Cart;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for POST /api/v1/orders endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {OrderController.class, GlobalExceptionHandler.class, CreateOrderUseCase.class})
@DisplayName("Create Order Contract Tests")
class CreateOrderContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private OrderRepository orderRepository;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid order creation request")
        void shouldAcceptValidOrderCreationRequest() throws Exception {
            Cart cart = createCartWithItems();
            when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "shippingAddress": {
                                        "recipientName": "Test User",
                                        "phone": "0912345678",
                                        "addressLine1": "123 Test Street",
                                        "city": "Taipei",
                                        "postalCode": "100",
                                        "country": "Taiwan"
                                    }
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should require shipping address")
        void shouldRequireShippingAddress() throws Exception {
            mockMvc.perform(post("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Response Contract - Success")
    class SuccessResponseContract {

        @Test
        @DisplayName("should return order with all required fields")
        void shouldReturnOrderWithAllRequiredFields() throws Exception {
            Cart cart = createCartWithItems();
            when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "shippingAddress": {
                                        "recipientName": "Test User",
                                        "phone": "0912345678",
                                        "addressLine1": "123 Test Street",
                                        "city": "Taipei",
                                        "postalCode": "100",
                                        "country": "Taiwan"
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.itemCount").isNumber())
                    .andExpect(jsonPath("$.total").isNumber())
                    .andExpect(jsonPath("$.currency").isString())
                    .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("should return order items with required fields")
        void shouldReturnOrderItemsWithRequiredFields() throws Exception {
            Cart cart = createCartWithItems();
            when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "shippingAddress": {
                                        "recipientName": "Test User",
                                        "phone": "0912345678",
                                        "addressLine1": "123 Test Street",
                                        "city": "Taipei",
                                        "postalCode": "100",
                                        "country": "Taiwan"
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].id").exists())
                    .andExpect(jsonPath("$.items[0].productId").exists())
                    .andExpect(jsonPath("$.items[0].productName").isString())
                    .andExpect(jsonPath("$.items[0].quantity").isNumber())
                    .andExpect(jsonPath("$.items[0].unitPrice").isNumber())
                    .andExpect(jsonPath("$.items[0].subtotal").isNumber());
        }
    }

    @Nested
    @DisplayName("Response Contract - Failure")
    class FailureResponseContract {

        @Test
        @DisplayName("should return 400 when cart is empty")
        void shouldReturn400WhenCartIsEmpty() throws Exception {
            Cart emptyCart = Cart.create(CUSTOMER_ID);
            when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(emptyCart));

            mockMvc.perform(post("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "shippingAddress": {
                                        "recipientName": "Test User",
                                        "phone": "0912345678",
                                        "addressLine1": "123 Test Street",
                                        "city": "Taipei",
                                        "postalCode": "100",
                                        "country": "Taiwan"
                                    }
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("CART_EMPTY"));
        }

        @Test
        @DisplayName("should return 400 when cart not found")
        void shouldReturn400WhenCartNotFound() throws Exception {
            when(cartRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/v1/orders")
                            .principal(() -> CUSTOMER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "shippingAddress": {
                                        "recipientName": "Test User",
                                        "phone": "0912345678",
                                        "addressLine1": "123 Test Street",
                                        "city": "Taipei",
                                        "postalCode": "100",
                                        "country": "Taiwan"
                                    }
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("CART_EMPTY"));
        }
    }

    @Nested
    @DisplayName("Get Order Contract")
    class GetOrderContract {

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
    }

    private Cart createCartWithItems() {
        Cart cart = Cart.create(CUSTOMER_ID);
        cart.addItem(
                PRODUCT_ID,
                "Test Product",
                "SKU-001",
                Money.of(new BigDecimal("999.00")),
                2
        );
        return cart;
    }
}
