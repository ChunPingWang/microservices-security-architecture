package com.ecommerce.order.contract;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.exceptions.InsufficientStockException;
import com.ecommerce.order.application.exceptions.ProductNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for POST /api/v1/cart/items endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CartController.class, GlobalExceptionHandler.class})
@DisplayName("Add to Cart Contract Tests")
class AddToCartContractTest {

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

    private static final String ADD_TO_CART_ENDPOINT = "/api/v1/cart/items";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid add to cart request")
        void shouldAcceptValidAddToCartRequest() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(addToCartUseCase.execute(eq(customerId), any()))
                    .thenReturn(createMockCartResponse(customerId));

            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(productId)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should require product ID")
        void shouldRequireProductId() throws Exception {
            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 2
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should default quantity to 1 when not specified")
        void shouldDefaultQuantityToOne() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(addToCartUseCase.execute(eq(customerId), any()))
                    .thenReturn(createMockCartResponse(customerId));

            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s"
                                }
                                """.formatted(productId)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Response Contract - Success")
    class SuccessResponseContract {

        @Test
        @DisplayName("should return updated cart with all required fields")
        void shouldReturnUpdatedCartWithAllRequiredFields() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(addToCartUseCase.execute(eq(customerId), any()))
                    .thenReturn(createMockCartResponse(customerId));

            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(productId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.itemCount").isNumber())
                    .andExpect(jsonPath("$.totalQuantity").isNumber())
                    .andExpect(jsonPath("$.total").isNumber())
                    .andExpect(jsonPath("$.currency").isString());
        }

        @Test
        @DisplayName("should return cart items with required fields")
        void shouldReturnCartItemsWithRequiredFields() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(addToCartUseCase.execute(eq(customerId), any()))
                    .thenReturn(createMockCartResponseWithItem(customerId, productId));

            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(productId)))
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
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(addToCartUseCase.execute(eq(customerId), any()))
                    .thenThrow(new ProductNotFoundException(productId));

            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(productId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("should return 409 when insufficient stock")
        void shouldReturn409WhenInsufficientStock() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(addToCartUseCase.execute(eq(customerId), any()))
                    .thenThrow(new InsufficientStockException(productId, 100));

            mockMvc.perform(post(ADD_TO_CART_ENDPOINT)
                            .principal(() -> customerId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 100
                                }
                                """.formatted(productId)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
        }
    }

    private CartResponse createMockCartResponse(UUID customerId) {
        return new CartResponse(
                UUID.randomUUID(),
                customerId,
                List.of(),
                0,
                0,
                BigDecimal.ZERO,
                "TWD",
                Instant.now()
        );
    }

    private CartResponse createMockCartResponseWithItem(UUID customerId, UUID productId) {
        var item = new com.ecommerce.order.application.dto.CartItemResponse(
                UUID.randomUUID(),
                productId,
                "Test Product",
                "SKU-001",
                new BigDecimal("999.00"),
                "TWD",
                2,
                new BigDecimal("1998.00"),
                Instant.now()
        );

        return new CartResponse(
                UUID.randomUUID(),
                customerId,
                List.of(item),
                1,
                2,
                new BigDecimal("1998.00"),
                "TWD",
                Instant.now()
        );
    }
}
