package com.ecommerce.order.unit.domain;

import com.ecommerce.order.domain.aggregates.Cart;
import com.ecommerce.order.domain.entities.CartItem;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Cart Aggregate")
class CartTest {

    private UUID customerId;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        cart = Cart.create(customerId);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create empty cart for customer")
        void shouldCreateEmptyCartForCustomer() {
            assertThat(cart.getCustomerId()).isEqualTo(customerId);
            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.getItemCount()).isZero();
            assertThat(cart.getId()).isNotNull();
        }

        @Test
        @DisplayName("should reject null customer ID")
        void shouldRejectNullCustomerId() {
            assertThatThrownBy(() -> Cart.create(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Customer ID");
        }
    }

    @Nested
    @DisplayName("Add Item")
    class AddItem {

        @Test
        @DisplayName("should add new item to cart")
        void shouldAddNewItemToCart() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("999.00"));

            CartItem item = cart.addItem(productId, "Test Product", "SKU-001", price, 2);

            assertThat(cart.isEmpty()).isFalse();
            assertThat(cart.getItemCount()).isEqualTo(1);
            assertThat(item.getProductId()).isEqualTo(productId);
            assertThat(item.getQuantityValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("should increase quantity when adding existing product")
        void shouldIncreaseQuantityWhenAddingExistingProduct() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("999.00"));

            cart.addItem(productId, "Test Product", "SKU-001", price, 2);
            cart.addItem(productId, "Test Product", "SKU-001", price, 3);

            assertThat(cart.getItemCount()).isEqualTo(1);
            assertThat(cart.getItem(productId).get().getQuantityValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("should reject quantity exceeding maximum")
        void shouldRejectQuantityExceedingMaximum() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("999.00"));

            cart.addItem(productId, "Test Product", "SKU-001", price, 50);

            assertThatThrownBy(() -> cart.addItem(productId, "Test Product", "SKU-001", price, 50))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Maximum quantity");
        }
    }

    @Nested
    @DisplayName("Update Item")
    class UpdateItem {

        @Test
        @DisplayName("should update item quantity")
        void shouldUpdateItemQuantity() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("999.00"));
            cart.addItem(productId, "Test Product", "SKU-001", price, 2);

            cart.updateItemQuantity(productId, 5);

            assertThat(cart.getItem(productId).get().getQuantityValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("should reject update for non-existent product")
        void shouldRejectUpdateForNonExistentProduct() {
            UUID nonExistentProductId = UUID.randomUUID();

            assertThatThrownBy(() -> cart.updateItemQuantity(nonExistentProductId, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should reject invalid quantity")
        void shouldRejectInvalidQuantity() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("999.00"));
            cart.addItem(productId, "Test Product", "SKU-001", price, 2);

            assertThatThrownBy(() -> cart.updateItemQuantity(productId, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Remove Item")
    class RemoveItem {

        @Test
        @DisplayName("should remove item from cart")
        void shouldRemoveItemFromCart() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("999.00"));
            cart.addItem(productId, "Test Product", "SKU-001", price, 2);

            cart.removeItem(productId);

            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.containsProduct(productId)).isFalse();
        }

        @Test
        @DisplayName("should reject remove for non-existent product")
        void shouldRejectRemoveForNonExistentProduct() {
            UUID nonExistentProductId = UUID.randomUUID();

            assertThatThrownBy(() -> cart.removeItem(nonExistentProductId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Calculate Total")
    class CalculateTotal {

        @Test
        @DisplayName("should calculate total for empty cart")
        void shouldCalculateTotalForEmptyCart() {
            assertThat(cart.getTotal().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should calculate total for single item")
        void shouldCalculateTotalForSingleItem() {
            UUID productId = UUID.randomUUID();
            Money price = Money.of(new BigDecimal("100.00"));
            cart.addItem(productId, "Test Product", "SKU-001", price, 3);

            Money total = cart.getTotal();

            assertThat(total.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("should calculate total for multiple items")
        void shouldCalculateTotalForMultipleItems() {
            cart.addItem(UUID.randomUUID(), "Product A", "SKU-A", Money.of(new BigDecimal("100.00")), 2);
            cart.addItem(UUID.randomUUID(), "Product B", "SKU-B", Money.of(new BigDecimal("50.00")), 3);

            Money total = cart.getTotal();

            // 100*2 + 50*3 = 200 + 150 = 350
            assertThat(total.getAmount()).isEqualByComparingTo(new BigDecimal("350.00"));
        }
    }

    @Nested
    @DisplayName("Clear Cart")
    class ClearCart {

        @Test
        @DisplayName("should clear all items from cart")
        void shouldClearAllItemsFromCart() {
            cart.addItem(UUID.randomUUID(), "Product A", "SKU-A", Money.of(new BigDecimal("100.00")), 2);
            cart.addItem(UUID.randomUUID(), "Product B", "SKU-B", Money.of(new BigDecimal("50.00")), 3);

            cart.clear();

            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.getItemCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Item Count")
    class ItemCount {

        @Test
        @DisplayName("should return total quantity of all items")
        void shouldReturnTotalQuantityOfAllItems() {
            cart.addItem(UUID.randomUUID(), "Product A", "SKU-A", Money.of(new BigDecimal("100.00")), 2);
            cart.addItem(UUID.randomUUID(), "Product B", "SKU-B", Money.of(new BigDecimal("50.00")), 3);

            assertThat(cart.getTotalItemCount()).isEqualTo(5);
            assertThat(cart.getItemCount()).isEqualTo(2); // distinct products
        }
    }
}
