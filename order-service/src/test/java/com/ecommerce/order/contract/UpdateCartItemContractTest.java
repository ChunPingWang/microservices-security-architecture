package com.ecommerce.order.contract;

import com.ecommerce.order.application.dto.CartItemResponse;
import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.exceptions.CartItemNotFoundException;
import com.ecommerce.order.application.exceptions.InsufficientStockException;
import com.ecommerce.order.application.usecases.AddToCartUseCase;
import com.ecommerce.order.application.usecases.GetCartUseCase;
import com.ecommerce.order.application.usecases.RemoveCartItemUseCase;
import com.ecommerce.order.application.usecases.UpdateCartItemUseCase;
import com.ecommerce.order.infrastructure.web.GlobalExceptionHandler;
import com.ecommerce.order.infrastructure.web.controllers.CartController;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for PUT /api/v1/cart/items/{productId} endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CartController.class, GlobalExceptionHandler.class})
@DisplayName("Update Cart Item Contract Tests")
class UpdateCartItemContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddToCartUseCase addToCartUseCase;

    @MockBean
    private UpdateCartItemUseCase updateCartItemUseCase;

    @MockBean
    private RemoveCartItemUseCase removeCartItemUseCase;

    @MockBean
    private GetCartUseCase getCartUseCase;

    private static final String UPDATE_CART_ITEM_ENDPOINT = "/api/v1/cart/items/{productId}";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid quantity update")
        void shouldAcceptValidQuantityUpdate() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(updateCartItemUseCase.execute(eq(customerId), eq(productId), any()))
                    .thenReturn(createMockCartResponse(customerId, productId));

            mockMvc.perform(put(UPDATE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should reject quantity less than 1")
        void shouldRejectQuantityLessThanOne() throws Exception {
            UUID productId = UUID.randomUUID();

            mockMvc.perform(put(UPDATE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 0
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should reject quantity greater than 99")
        void shouldRejectQuantityGreaterThan99() throws Exception {
            UUID productId = UUID.randomUUID();

            mockMvc.perform(put(UPDATE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 100
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Response Contract - Success")
    class SuccessResponseContract {

        @Test
        @DisplayName("should return updated cart")
        void shouldReturnUpdatedCart() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(updateCartItemUseCase.execute(eq(customerId), eq(productId), any()))
                    .thenReturn(createMockCartResponse(customerId, productId));

            mockMvc.perform(put(UPDATE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.total").isNumber());
        }
    }

    @Nested
    @DisplayName("Response Contract - Failure")
    class FailureResponseContract {

        @Test
        @DisplayName("should return 404 when cart item not found")
        void shouldReturn404WhenCartItemNotFound() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(updateCartItemUseCase.execute(eq(customerId), eq(productId), any()))
                    .thenThrow(new CartItemNotFoundException(productId));

            mockMvc.perform(put(UPDATE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"));
        }

        @Test
        @DisplayName("should return 409 when insufficient stock")
        void shouldReturn409WhenInsufficientStock() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(updateCartItemUseCase.execute(eq(customerId), eq(productId), any()))
                    .thenThrow(new InsufficientStockException(productId, 50));

            mockMvc.perform(put(UPDATE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 50
                                }
                                """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
        }
    }

    private CartResponse createMockCartResponse(UUID customerId, UUID productId) {
        var item = new CartItemResponse(
                UUID.randomUUID(),
                productId,
                "Test Product",
                "SKU-001",
                new BigDecimal("999.00"),
                "TWD",
                5,
                new BigDecimal("4995.00"),
                Instant.now()
        );

        return new CartResponse(
                UUID.randomUUID(),
                customerId,
                List.of(item),
                1,
                5,
                new BigDecimal("4995.00"),
                "TWD",
                Instant.now()
        );
    }
}
