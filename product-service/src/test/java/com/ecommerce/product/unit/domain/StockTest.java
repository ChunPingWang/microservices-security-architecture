package com.ecommerce.product.unit.domain;

import com.ecommerce.product.domain.value_objects.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Stock value object.
 */
@DisplayName("Stock Value Object")
class StockTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create stock with valid quantity")
        void shouldCreateWithValidQuantity() {
            Stock stock = Stock.of(100);
            assertThat(stock.getQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("should create zero stock")
        void shouldCreateZeroStock() {
            Stock stock = Stock.zero();
            assertThat(stock.getQuantity()).isZero();
            assertThat(stock.isOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("should reject negative quantity")
        void shouldRejectNegativeQuantity() {
            assertThatThrownBy(() -> Stock.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }
    }

    @Nested
    @DisplayName("Stock Operations")
    class StockOperations {

        @Test
        @DisplayName("should add stock")
        void shouldAddStock() {
            Stock stock = Stock.of(10);
            Stock newStock = stock.add(5);

            assertThat(newStock.getQuantity()).isEqualTo(15);
            assertThat(stock.getQuantity()).isEqualTo(10); // Immutable
        }

        @Test
        @DisplayName("should subtract stock")
        void shouldSubtractStock() {
            Stock stock = Stock.of(10);
            Stock newStock = stock.subtract(3);

            assertThat(newStock.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("should reject subtraction resulting in negative")
        void shouldRejectNegativeResult() {
            Stock stock = Stock.of(5);

            assertThatThrownBy(() -> stock.subtract(10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("should reject negative addition")
        void shouldRejectNegativeAddition() {
            Stock stock = Stock.of(10);

            assertThatThrownBy(() -> stock.add(-5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("Stock Status")
    class StockStatus {

        @Test
        @DisplayName("should detect out of stock")
        void shouldDetectOutOfStock() {
            Stock stock = Stock.of(0);
            assertThat(stock.isOutOfStock()).isTrue();
            assertThat(stock.isLowStock(10)).isTrue();
        }

        @Test
        @DisplayName("should detect low stock")
        void shouldDetectLowStock() {
            Stock stock = Stock.of(5);
            assertThat(stock.isLowStock(10)).isTrue();
            assertThat(stock.isLowStock(5)).isFalse();
        }

        @Test
        @DisplayName("should check availability")
        void shouldCheckAvailability() {
            Stock stock = Stock.of(10);

            assertThat(stock.hasEnough(5)).isTrue();
            assertThat(stock.hasEnough(10)).isTrue();
            assertThat(stock.hasEnough(11)).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same quantity")
        void shouldBeEqualForSameQuantity() {
            Stock stock1 = Stock.of(50);
            Stock stock2 = Stock.of(50);

            assertThat(stock1).isEqualTo(stock2);
            assertThat(stock1.hashCode()).isEqualTo(stock2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different quantities")
        void shouldNotBeEqualForDifferentQuantities() {
            Stock stock1 = Stock.of(50);
            Stock stock2 = Stock.of(100);

            assertThat(stock1).isNotEqualTo(stock2);
        }
    }
}
