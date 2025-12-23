package com.ecommerce.product.unit.domain;

import com.ecommerce.product.domain.value_objects.SKU;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SKU value object.
 */
@DisplayName("SKU Value Object")
class SKUTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create SKU with valid value")
        void shouldCreateWithValidValue() {
            SKU sku = SKU.of("PROD-001-BLK-M");
            assertThat(sku.getValue()).isEqualTo("PROD-001-BLK-M");
        }

        @Test
        @DisplayName("should normalize SKU to uppercase")
        void shouldNormalizeToUppercase() {
            SKU sku = SKU.of("prod-001-blk-m");
            assertThat(sku.getValue()).isEqualTo("PROD-001-BLK-M");
        }

        @Test
        @DisplayName("should trim whitespace")
        void shouldTrimWhitespace() {
            SKU sku = SKU.of("  PROD-001  ");
            assertThat(sku.getValue()).isEqualTo("PROD-001");
        }

        @Test
        @DisplayName("should generate random SKU")
        void shouldGenerateRandomSku() {
            SKU sku = SKU.generate("SHIRT");
            assertThat(sku.getValue()).startsWith("SHIRT-");
            assertThat(sku.getValue()).hasSize(14); // SHIRT- + 8 chars
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should reject null or empty SKU")
        void shouldRejectNullOrEmpty(String invalidSku) {
            assertThatThrownBy(() -> SKU.of(invalidSku))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"AB", "A"})
        @DisplayName("should reject SKU shorter than 3 characters")
        void shouldRejectTooShort(String shortSku) {
            assertThatThrownBy(() -> SKU.of(shortSku))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 3 characters");
        }

        @Test
        @DisplayName("should reject SKU longer than 50 characters")
        void shouldRejectTooLong() {
            String longSku = "A".repeat(51);
            assertThatThrownBy(() -> SKU.of(longSku))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceed 50 characters");
        }

        @ParameterizedTest
        @ValueSource(strings = {"PROD@001", "PROD#001", "PROD 001", "PROD.001"})
        @DisplayName("should reject SKU with invalid characters")
        void shouldRejectInvalidCharacters(String invalidSku) {
            assertThatThrownBy(() -> SKU.of(invalidSku))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("alphanumeric characters and hyphens");
        }

        @ParameterizedTest
        @ValueSource(strings = {"PROD-001", "ABC123", "ITEM-A1-B2", "SKU123456"})
        @DisplayName("should accept valid SKU formats")
        void shouldAcceptValidFormats(String validSku) {
            SKU sku = SKU.of(validSku);
            assertThat(sku.getValue()).isEqualTo(validSku.toUpperCase());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same SKU value")
        void shouldBeEqualForSameValue() {
            SKU sku1 = SKU.of("PROD-001");
            SKU sku2 = SKU.of("PROD-001");
            assertThat(sku1).isEqualTo(sku2);
            assertThat(sku1.hashCode()).isEqualTo(sku2.hashCode());
        }

        @Test
        @DisplayName("should be equal regardless of case")
        void shouldBeEqualRegardlessOfCase() {
            SKU sku1 = SKU.of("PROD-001");
            SKU sku2 = SKU.of("prod-001");
            assertThat(sku1).isEqualTo(sku2);
        }

        @Test
        @DisplayName("should not be equal for different SKU values")
        void shouldNotBeEqualForDifferentValues() {
            SKU sku1 = SKU.of("PROD-001");
            SKU sku2 = SKU.of("PROD-002");
            assertThat(sku1).isNotEqualTo(sku2);
        }
    }
}
