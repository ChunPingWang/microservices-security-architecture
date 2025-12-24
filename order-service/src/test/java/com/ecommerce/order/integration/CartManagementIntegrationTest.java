package com.ecommerce.order.integration;

import com.ecommerce.order.domain.ports.CartRepository;
import com.ecommerce.order.domain.ports.ProductServicePort;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for cart management flow.
 * Tests the complete flow: add items -> update quantity -> get cart -> remove items.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DisplayName("Cart Management Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private ProductServicePort productService;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_1_ID = UUID.randomUUID();
    private static final UUID PRODUCT_2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Mock product service responses
        when(productService.getProductInfo(PRODUCT_1_ID))
                .thenReturn(Optional.of(new ProductServicePort.ProductInfo(
                        PRODUCT_1_ID,
                        "Test Product 1",
                        "SKU-001",
                        Money.of(new BigDecimal("999.00")),
                        100,
                        true
                )));
        when(productService.getProductInfo(PRODUCT_2_ID))
                .thenReturn(Optional.of(new ProductServicePort.ProductInfo(
                        PRODUCT_2_ID,
                        "Test Product 2",
                        "SKU-002",
                        Money.of(new BigDecimal("599.00")),
                        100,
                        true
                )));
        when(productService.isStockAvailable(any(UUID.class), anyInt())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        // Clean up the cart after each test
        cartRepository.findByCustomerId(CUSTOMER_ID)
                .ifPresent(cart -> {
                    cart.clear();
                    cartRepository.save(cart);
                });
    }

    @Nested
    @DisplayName("Complete Shopping Flow")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CompleteShoppingFlow {

        @Test
        @Order(1)
        @DisplayName("should start with empty cart")
        void shouldStartWithEmptyCart() throws Exception {
            mockMvc.perform(get("/api/v1/cart")
                            .with(user(CUSTOMER_ID.toString())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.itemCount").value(0))
                    .andExpect(jsonPath("$.totalQuantity").value(0))
                    .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @Order(2)
        @DisplayName("should add first item to cart")
        void shouldAddFirstItemToCart() throws Exception {
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.itemCount").value(1))
                    .andExpect(jsonPath("$.totalQuantity").value(2))
                    .andExpect(jsonPath("$.items[0].productId").value(PRODUCT_1_ID.toString()))
                    .andExpect(jsonPath("$.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.items[0].productName").value("Test Product 1"));
        }

        @Test
        @Order(3)
        @DisplayName("should add second item to cart")
        void shouldAddSecondItemToCart() throws Exception {
            // First add product 1
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk());

            // Then add product 2
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 3
                                }
                                """.formatted(PRODUCT_2_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemCount").value(2))
                    .andExpect(jsonPath("$.totalQuantity").value(5));
        }

        @Test
        @Order(4)
        @DisplayName("should update item quantity")
        void shouldUpdateItemQuantity() throws Exception {
            // Add product 1 first
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk());

            // Update quantity
            mockMvc.perform(put("/api/v1/cart/items/{productId}", PRODUCT_1_ID)
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantity").value(5))
                    .andExpect(jsonPath("$.totalQuantity").value(5));
        }

        @Test
        @Order(5)
        @DisplayName("should remove item from cart")
        void shouldRemoveItemFromCart() throws Exception {
            // Add two products
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 3
                                }
                                """.formatted(PRODUCT_2_ID)))
                    .andExpect(status().isOk());

            // Remove first product
            mockMvc.perform(delete("/api/v1/cart/items/{productId}", PRODUCT_1_ID)
                            .with(user(CUSTOMER_ID.toString())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemCount").value(1))
                    .andExpect(jsonPath("$.totalQuantity").value(3));
        }
    }

    @Nested
    @DisplayName("Add To Cart Scenarios")
    class AddToCartScenarios {

        @Test
        @DisplayName("should increase quantity when adding same product twice")
        void shouldIncreaseQuantityWhenAddingSameProductTwice() throws Exception {
            // Add product once
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantity").value(2));

            // Add same product again
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 3
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemCount").value(1))
                    .andExpect(jsonPath("$.items[0].quantity").value(5));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            UUID unknownProductId = UUID.randomUUID();
            when(productService.getProductInfo(unknownProductId)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 1
                                }
                                """.formatted(unknownProductId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("should return 409 when insufficient stock")
        void shouldReturn409WhenInsufficientStock() throws Exception {
            when(productService.isStockAvailable(PRODUCT_1_ID, 50)).thenReturn(false);

            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 50
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
        }
    }

    @Nested
    @DisplayName("Update Cart Item Scenarios")
    class UpdateCartItemScenarios {

        @Test
        @DisplayName("should return 404 when updating non-existent item")
        void shouldReturn404WhenUpdatingNonExistentItem() throws Exception {
            mockMvc.perform(put("/api/v1/cart/items/{productId}", PRODUCT_1_ID)
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Remove Cart Item Scenarios")
    class RemoveCartItemScenarios {

        @Test
        @DisplayName("should return 404 when removing non-existent item")
        void shouldReturn404WhenRemovingNonExistentItem() throws Exception {
            mockMvc.perform(delete("/api/v1/cart/items/{productId}", PRODUCT_1_ID)
                            .with(user(CUSTOMER_ID.toString())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Cart Total Calculations")
    class CartTotalCalculations {

        @Test
        @DisplayName("should calculate cart total correctly")
        void shouldCalculateCartTotalCorrectly() throws Exception {
            // Add product 1: 2 x 999 = 1998
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 2
                                }
                                """.formatted(PRODUCT_1_ID)))
                    .andExpect(status().isOk());

            // Add product 2: 3 x 599 = 1797
            // Total: 1998 + 1797 = 3795
            mockMvc.perform(post("/api/v1/cart/items")
                            .with(user(CUSTOMER_ID.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "productId": "%s",
                                    "quantity": 3
                                }
                                """.formatted(PRODUCT_2_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(3795.0))
                    .andExpect(jsonPath("$.currency").value("TWD"));
        }
    }
}
