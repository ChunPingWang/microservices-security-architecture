package com.ecommerce.order.contract;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.exceptions.CartItemNotFoundException;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for DELETE /api/v1/cart/items/{productId} endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CartController.class, GlobalExceptionHandler.class})
@DisplayName("Remove Cart Item Contract Tests")
class RemoveCartItemContractTest {

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

    private static final String REMOVE_CART_ITEM_ENDPOINT = "/api/v1/cart/items/{productId}";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid UUID for product ID")
        void shouldAcceptValidUuidForProductId() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(removeCartItemUseCase.execute(customerId, productId))
                    .thenReturn(createEmptyCartResponse(customerId));

            mockMvc.perform(delete(REMOVE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should reject invalid UUID format")
        void shouldRejectInvalidUuidFormat() throws Exception {
            mockMvc.perform(delete("/api/v1/cart/items/invalid-uuid")
                            .principal(() -> UUID.randomUUID().toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Response Contract - Success")
    class SuccessResponseContract {

        @Test
        @DisplayName("should return updated cart after removal")
        void shouldReturnUpdatedCartAfterRemoval() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(removeCartItemUseCase.execute(customerId, productId))
                    .thenReturn(createEmptyCartResponse(customerId));

            mockMvc.perform(delete(REMOVE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.itemCount").value(0))
                    .andExpect(jsonPath("$.total").value(0));
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
            when(removeCartItemUseCase.execute(customerId, productId))
                    .thenThrow(new CartItemNotFoundException(productId));

            mockMvc.perform(delete(REMOVE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("Error Response Contract")
    class ErrorResponseContract {

        @Test
        @DisplayName("should return standard error structure")
        void shouldReturnStandardErrorStructure() throws Exception {
            UUID customerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            when(removeCartItemUseCase.execute(customerId, productId))
                    .thenThrow(new CartItemNotFoundException(productId));

            mockMvc.perform(delete(REMOVE_CART_ITEM_ENDPOINT, productId)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.cause").doesNotExist());
        }
    }

    private CartResponse createEmptyCartResponse(UUID customerId) {
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
}
