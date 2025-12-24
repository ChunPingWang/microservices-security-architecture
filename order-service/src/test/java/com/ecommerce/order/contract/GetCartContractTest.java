package com.ecommerce.order.contract;

import com.ecommerce.order.application.dto.CartItemResponse;
import com.ecommerce.order.application.dto.CartResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/cart endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CartController.class, GlobalExceptionHandler.class})
@DisplayName("Get Cart Contract Tests")
class GetCartContractTest {

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

    private static final String GET_CART_ENDPOINT = "/api/v1/cart";

    @Nested
    @DisplayName("Response Contract - Empty Cart")
    class EmptyCartResponse {

        @Test
        @DisplayName("should return empty cart for new customer")
        void shouldReturnEmptyCartForNewCustomer() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(getCartUseCase.execute(customerId))
                    .thenReturn(CartResponse.empty(customerId));

            mockMvc.perform(get(GET_CART_ENDPOINT)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.itemCount").value(0))
                    .andExpect(jsonPath("$.totalQuantity").value(0))
                    .andExpect(jsonPath("$.total").value(0));
        }
    }

    @Nested
    @DisplayName("Response Contract - Cart with Items")
    class CartWithItemsResponse {

        @Test
        @DisplayName("should return cart with all required fields")
        void shouldReturnCartWithAllRequiredFields() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(getCartUseCase.execute(customerId))
                    .thenReturn(createMockCartWithItems(customerId));

            mockMvc.perform(get(GET_CART_ENDPOINT)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.itemCount").value(2))
                    .andExpect(jsonPath("$.totalQuantity").value(5))
                    .andExpect(jsonPath("$.total").isNumber())
                    .andExpect(jsonPath("$.currency").value("TWD"))
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("should return items with all required fields")
        void shouldReturnItemsWithAllRequiredFields() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(getCartUseCase.execute(customerId))
                    .thenReturn(createMockCartWithItems(customerId));

            mockMvc.perform(get(GET_CART_ENDPOINT)
                            .principal(() -> customerId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].id").exists())
                    .andExpect(jsonPath("$.items[0].productId").exists())
                    .andExpect(jsonPath("$.items[0].productName").isString())
                    .andExpect(jsonPath("$.items[0].productSku").isString())
                    .andExpect(jsonPath("$.items[0].unitPrice").isNumber())
                    .andExpect(jsonPath("$.items[0].currency").isString())
                    .andExpect(jsonPath("$.items[0].quantity").isNumber())
                    .andExpect(jsonPath("$.items[0].subtotal").isNumber())
                    .andExpect(jsonPath("$.items[0].addedAt").exists());
        }
    }

    private CartResponse createMockCartWithItems(UUID customerId) {
        var item1 = new CartItemResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Product A",
                "SKU-A",
                new BigDecimal("100.00"),
                "TWD",
                2,
                new BigDecimal("200.00"),
                Instant.now()
        );

        var item2 = new CartItemResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Product B",
                "SKU-B",
                new BigDecimal("150.00"),
                "TWD",
                3,
                new BigDecimal("450.00"),
                Instant.now()
        );

        return new CartResponse(
                UUID.randomUUID(),
                customerId,
                List.of(item1, item2),
                2,
                5,
                new BigDecimal("650.00"),
                "TWD",
                Instant.now()
        );
    }
}
