package com.ecommerce.order.unit.domain;

import com.ecommerce.order.domain.value_objects.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Quantity Value Object")
class QuantityTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create quantity with valid value")
        void shouldCreateQuantityWithValidValue() {
            Quantity quantity = Quantity.of(5);

            assertThat(quantity.getValue()).isEqualTo(5);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 50, 99})
        @DisplayName("should accept valid quantities")
        void shouldAcceptValidQuantities(int value) {
            Quantity quantity = Quantity.of(value);

            assertThat(quantity.getValue()).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("should reject quantity below minimum")
        void shouldRejectQuantityBelowMinimum(int value) {
            assertThatThrownBy(() -> Quantity.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least");
        }

        @ParameterizedTest
        @ValueSource(ints = {100, 150, 1000})
        @DisplayName("should reject quantity above maximum")
        void shouldRejectQuantityAboveMaximum(int value) {
            assertThatThrownBy(() -> Quantity.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceed");
        }
    }

    @Nested
    @DisplayName("Operations")
    class Operations {

        @Test
        @DisplayName("should add to quantity")
        void shouldAddToQuantity() {
            Quantity quantity = Quantity.of(5);

            Quantity result = quantity.add(3);

            assertThat(result.getValue()).isEqualTo(8);
        }

        @Test
        @DisplayName("should subtract from quantity")
        void shouldSubtractFromQuantity() {
            Quantity quantity = Quantity.of(5);

            Quantity result = quantity.subtract(3);

            assertThat(result.getValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("should reject addition exceeding maximum")
        void shouldRejectAdditionExceedingMaximum() {
            Quantity quantity = Quantity.of(95);

            assertThatThrownBy(() -> quantity.add(10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject subtraction below minimum")
        void shouldRejectSubtractionBelowMinimum() {
            Quantity quantity = Quantity.of(3);

            assertThatThrownBy(() -> quantity.subtract(5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same value")
        void shouldBeEqualForSameValue() {
            Quantity q1 = Quantity.of(5);
            Quantity q2 = Quantity.of(5);

            assertThat(q1).isEqualTo(q2);
            assertThat(q1.hashCode()).isEqualTo(q2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            Quantity q1 = Quantity.of(5);
            Quantity q2 = Quantity.of(10);

            assertThat(q1).isNotEqualTo(q2);
        }
    }
}
