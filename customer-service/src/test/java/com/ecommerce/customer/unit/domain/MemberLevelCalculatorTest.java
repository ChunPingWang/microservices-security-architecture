package com.ecommerce.customer.unit.domain;

import com.ecommerce.customer.domain.services.MemberLevelCalculator;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MemberLevelCalculator domain service.
 */
@DisplayName("Member Level Calculator")
class MemberLevelCalculatorTest {

    private final MemberLevelCalculator calculator = new MemberLevelCalculator();

    @Nested
    @DisplayName("Level Calculation")
    class LevelCalculation {

        @Test
        @DisplayName("should return NORMAL for spending below 10000")
        void shouldReturnNormalForSpendingBelow10000() {
            assertEquals(MemberLevel.NORMAL, calculator.calculateLevel(BigDecimal.ZERO));
            assertEquals(MemberLevel.NORMAL, calculator.calculateLevel(new BigDecimal("5000")));
            assertEquals(MemberLevel.NORMAL, calculator.calculateLevel(new BigDecimal("9999.99")));
        }

        @Test
        @DisplayName("should return SILVER for spending 10000-29999")
        void shouldReturnSilverForSpending10000To29999() {
            assertEquals(MemberLevel.SILVER, calculator.calculateLevel(new BigDecimal("10000")));
            assertEquals(MemberLevel.SILVER, calculator.calculateLevel(new BigDecimal("20000")));
            assertEquals(MemberLevel.SILVER, calculator.calculateLevel(new BigDecimal("29999.99")));
        }

        @Test
        @DisplayName("should return GOLD for spending 30000-99999")
        void shouldReturnGoldForSpending30000To99999() {
            assertEquals(MemberLevel.GOLD, calculator.calculateLevel(new BigDecimal("30000")));
            assertEquals(MemberLevel.GOLD, calculator.calculateLevel(new BigDecimal("50000")));
            assertEquals(MemberLevel.GOLD, calculator.calculateLevel(new BigDecimal("99999.99")));
        }

        @Test
        @DisplayName("should return PLATINUM for spending 100000 or above")
        void shouldReturnPlatinumForSpending100000OrAbove() {
            assertEquals(MemberLevel.PLATINUM, calculator.calculateLevel(new BigDecimal("100000")));
            assertEquals(MemberLevel.PLATINUM, calculator.calculateLevel(new BigDecimal("200000")));
        }
    }

    @Nested
    @DisplayName("Next Level Calculation")
    class NextLevelCalculation {

        @Test
        @DisplayName("should calculate spending needed to reach SILVER from NORMAL")
        void shouldCalculateSpendingNeededForSilver() {
            BigDecimal spendingNeeded = calculator.spendingToNextLevel(new BigDecimal("5000"));
            assertEquals(new BigDecimal("5000"), spendingNeeded);
        }

        @Test
        @DisplayName("should calculate spending needed to reach GOLD from SILVER")
        void shouldCalculateSpendingNeededForGold() {
            BigDecimal spendingNeeded = calculator.spendingToNextLevel(new BigDecimal("15000"));
            assertEquals(new BigDecimal("15000"), spendingNeeded);
        }

        @Test
        @DisplayName("should calculate spending needed to reach PLATINUM from GOLD")
        void shouldCalculateSpendingNeededForPlatinum() {
            BigDecimal spendingNeeded = calculator.spendingToNextLevel(new BigDecimal("50000"));
            assertEquals(new BigDecimal("50000"), spendingNeeded);
        }

        @Test
        @DisplayName("should return zero for PLATINUM members")
        void shouldReturnZeroForPlatinumMembers() {
            BigDecimal spendingNeeded = calculator.spendingToNextLevel(new BigDecimal("150000"));
            assertTrue(spendingNeeded.compareTo(BigDecimal.ZERO) == 0);
        }
    }

    @Nested
    @DisplayName("Benefits Calculation")
    class BenefitsCalculation {

        @Test
        @DisplayName("should return 0% discount for NORMAL members")
        void shouldReturnZeroDiscountForNormal() {
            int discount = calculator.getDiscountPercentage(MemberLevel.NORMAL);
            assertEquals(0, discount);
        }

        @Test
        @DisplayName("should return 3% discount for SILVER members")
        void shouldReturn3PercentDiscountForSilver() {
            int discount = calculator.getDiscountPercentage(MemberLevel.SILVER);
            assertEquals(3, discount);
        }

        @Test
        @DisplayName("should return 5% discount for GOLD members")
        void shouldReturn5PercentDiscountForGold() {
            int discount = calculator.getDiscountPercentage(MemberLevel.GOLD);
            assertEquals(5, discount);
        }

        @Test
        @DisplayName("should return 10% discount for PLATINUM members")
        void shouldReturn10PercentDiscountForPlatinum() {
            int discount = calculator.getDiscountPercentage(MemberLevel.PLATINUM);
            assertEquals(10, discount);
        }
    }

    @Nested
    @DisplayName("Level Upgrade Check")
    class LevelUpgradeCheck {

        @Test
        @DisplayName("should detect level upgrade when crossing threshold")
        void shouldDetectLevelUpgrade() {
            assertTrue(calculator.wouldUpgrade(MemberLevel.NORMAL, new BigDecimal("10000")));
            assertTrue(calculator.wouldUpgrade(MemberLevel.SILVER, new BigDecimal("30000")));
            assertTrue(calculator.wouldUpgrade(MemberLevel.GOLD, new BigDecimal("100000")));
        }

        @Test
        @DisplayName("should not detect upgrade when staying at same level")
        void shouldNotDetectUpgradeWhenSameLevel() {
            assertFalse(calculator.wouldUpgrade(MemberLevel.NORMAL, new BigDecimal("5000")));
            assertFalse(calculator.wouldUpgrade(MemberLevel.SILVER, new BigDecimal("20000")));
            assertFalse(calculator.wouldUpgrade(MemberLevel.GOLD, new BigDecimal("50000")));
        }

        @Test
        @DisplayName("should not upgrade from PLATINUM")
        void shouldNotUpgradeFromPlatinum() {
            assertFalse(calculator.wouldUpgrade(MemberLevel.PLATINUM, new BigDecimal("200000")));
        }
    }
}
