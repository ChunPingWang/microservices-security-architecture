package com.ecommerce.product.unit.domain;

import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.value_objects.SKU;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Product entity.
 */
@DisplayName("Product Entity")
class ProductTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create product with valid data")
        void shouldCreateWithValidData() {
            SKU sku = SKU.of("SHIRT-001");
            Money price = Money.of(new BigDecimal("299.00"));
            UUID categoryId = UUID.randomUUID();

            Product product = Product.create(
                    sku,
                    "Premium Cotton Shirt",
                    "High-quality cotton shirt",
                    price,
                    categoryId
            );

            assertThat(product.getId()).isNotNull();
            assertThat(product.getSku()).isEqualTo(sku);
            assertThat(product.getName()).isEqualTo("Premium Cotton Shirt");
            assertThat(product.getDescription()).isEqualTo("High-quality cotton shirt");
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getCategoryId()).isEqualTo(categoryId);
            assertThat(product.isActive()).isTrue();
        }

        @Test
        @DisplayName("should reject null SKU")
        void shouldRejectNullSku() {
            Money price = Money.of(new BigDecimal("299.00"));
            UUID categoryId = UUID.randomUUID();

            assertThatThrownBy(() -> Product.create(
                    null,
                    "Test Product",
                    "Description",
                    price,
                    categoryId
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("SKU");
        }

        @Test
        @DisplayName("should reject null name")
        void shouldRejectNullName() {
            SKU sku = SKU.of("TEST-001");
            Money price = Money.of(new BigDecimal("299.00"));
            UUID categoryId = UUID.randomUUID();

            assertThatThrownBy(() -> Product.create(
                    sku,
                    null,
                    "Description",
                    price,
                    categoryId
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("should reject null price")
        void shouldRejectNullPrice() {
            SKU sku = SKU.of("TEST-001");
            UUID categoryId = UUID.randomUUID();

            assertThatThrownBy(() -> Product.create(
                    sku,
                    "Test Product",
                    "Description",
                    null,
                    categoryId
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Price");
        }
    }

    @Nested
    @DisplayName("Price Management")
    class PriceManagement {

        @Test
        @DisplayName("should update price")
        void shouldUpdatePrice() {
            Product product = createTestProduct();
            Money newPrice = Money.of(new BigDecimal("399.00"));

            product.updatePrice(newPrice);

            assertThat(product.getPrice()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("should reject negative price")
        void shouldRejectNegativePrice() {
            // Negative price validation happens at Money creation time
            assertThatThrownBy(() -> Money.of(new BigDecimal("-100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("should reject null price update")
        void shouldRejectNullPriceUpdate() {
            Product product = createTestProduct();

            assertThatThrownBy(() -> product.updatePrice(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Product Status")
    class ProductStatus {

        @Test
        @DisplayName("should deactivate product")
        void shouldDeactivateProduct() {
            Product product = createTestProduct();
            assertThat(product.isActive()).isTrue();

            product.deactivate();

            assertThat(product.isActive()).isFalse();
        }

        @Test
        @DisplayName("should activate product")
        void shouldActivateProduct() {
            Product product = createTestProduct();
            product.deactivate();
            assertThat(product.isActive()).isFalse();

            product.activate();

            assertThat(product.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Product Details Update")
    class ProductDetailsUpdate {

        @Test
        @DisplayName("should update name and description")
        void shouldUpdateNameAndDescription() {
            Product product = createTestProduct();

            product.updateDetails("New Name", "New Description");

            assertThat(product.getName()).isEqualTo("New Name");
            assertThat(product.getDescription()).isEqualTo("New Description");
        }

        @Test
        @DisplayName("should update only name when description is null")
        void shouldUpdateOnlyName() {
            Product product = createTestProduct();
            String originalDescription = product.getDescription();

            product.updateDetails("New Name", null);

            assertThat(product.getName()).isEqualTo("New Name");
            assertThat(product.getDescription()).isEqualTo(originalDescription);
        }

        @Test
        @DisplayName("should reject blank name")
        void shouldRejectBlankName() {
            Product product = createTestProduct();

            assertThatThrownBy(() -> product.updateDetails("  ", "Description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }
    }

    @Nested
    @DisplayName("Category Assignment")
    class CategoryAssignment {

        @Test
        @DisplayName("should change category")
        void shouldChangeCategory() {
            Product product = createTestProduct();
            UUID newCategoryId = UUID.randomUUID();

            product.changeCategory(newCategoryId);

            assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
        }

        @Test
        @DisplayName("should reject null category")
        void shouldRejectNullCategory() {
            Product product = createTestProduct();

            assertThatThrownBy(() -> product.changeCategory(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private Product createTestProduct() {
        return Product.create(
                SKU.of("TEST-001"),
                "Test Product",
                "Test Description",
                Money.of(new BigDecimal("100.00")),
                UUID.randomUUID()
        );
    }
}
