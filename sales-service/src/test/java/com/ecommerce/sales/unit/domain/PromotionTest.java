package com.ecommerce.sales.unit.domain;

import com.ecommerce.sales.domain.aggregates.Promotion;
import com.ecommerce.sales.domain.value_objects.DiscountRule;
import com.ecommerce.sales.domain.value_objects.DiscountType;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Promotion aggregate.
 */
@DisplayName("Promotion Aggregate")
class PromotionTest {

    @Nested
    @DisplayName("Promotion Creation")
    class PromotionCreation {

        @Test
        @DisplayName("should create promotion with percentage discount")
        void shouldCreatePromotionWithPercentageDiscount() {
            DiscountRule rule = DiscountRule.percentage(20);
            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "雙11特賣",
                    "全站商品8折優惠",
                    rule,
                    startDate,
                    endDate
            );

            assertNotNull(promotion.getId());
            assertEquals("雙11特賣", promotion.getName());
            assertEquals("全站商品8折優惠", promotion.getDescription());
            assertEquals(DiscountType.PERCENTAGE, promotion.getDiscountRule().type());
            assertEquals(new BigDecimal("20"), promotion.getDiscountRule().value());
            assertTrue(promotion.isActive());
        }

        @Test
        @DisplayName("should create promotion with fixed amount discount")
        void shouldCreatePromotionWithFixedAmountDiscount() {
            DiscountRule rule = DiscountRule.fixedAmount(Money.of(new BigDecimal("100")));
            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "滿額折扣",
                    "訂單滿1000折100",
                    rule,
                    startDate,
                    endDate
            );

            assertEquals(DiscountType.FIXED_AMOUNT, promotion.getDiscountRule().type());
            assertEquals(new BigDecimal("100.00"), promotion.getDiscountRule().value());
        }

        @Test
        @DisplayName("should fail when name is null")
        void shouldFailWhenNameIsNull() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now();
            Instant endDate = startDate.plus(7, ChronoUnit.DAYS);

            assertThrows(NullPointerException.class, () ->
                    Promotion.create(null, "描述", rule, startDate, endDate)
            );
        }

        @Test
        @DisplayName("should fail when end date is before start date")
        void shouldFailWhenEndDateBeforeStartDate() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now();
            Instant endDate = startDate.minus(1, ChronoUnit.DAYS);

            assertThrows(IllegalArgumentException.class, () ->
                    Promotion.create("促銷", "描述", rule, startDate, endDate)
            );
        }
    }

    @Nested
    @DisplayName("Promotion Status")
    class PromotionStatus {

        @Test
        @DisplayName("should be active when within date range")
        void shouldBeActiveWhenWithinDateRange() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "進行中促銷", "描述", rule, startDate, endDate
            );

            assertTrue(promotion.isActive());
            assertFalse(promotion.isExpired());
        }

        @Test
        @DisplayName("should not be active when before start date")
        void shouldNotBeActiveWhenBeforeStartDate() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "未開始促銷", "描述", rule, startDate, endDate
            );

            assertFalse(promotion.isActive());
            assertFalse(promotion.isExpired());
        }

        @Test
        @DisplayName("should be expired when after end date")
        void shouldBeExpiredWhenAfterEndDate() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now().minus(14, ChronoUnit.DAYS);
            Instant endDate = Instant.now().minus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "已過期促銷", "描述", rule, startDate, endDate
            );

            assertFalse(promotion.isActive());
            assertTrue(promotion.isExpired());
        }

        @Test
        @DisplayName("should be deactivated when manually disabled")
        void shouldBeDeactivatedWhenManuallyDisabled() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "促銷", "描述", rule, startDate, endDate
            );
            promotion.deactivate();

            assertFalse(promotion.isActive());
            assertTrue(promotion.isManuallyDeactivated());
        }

        @Test
        @DisplayName("should be reactivated after deactivation")
        void shouldBeReactivatedAfterDeactivation() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "促銷", "描述", rule, startDate, endDate
            );
            promotion.deactivate();
            promotion.activate();

            assertTrue(promotion.isActive());
        }
    }

    @Nested
    @DisplayName("Discount Calculation")
    class DiscountCalculation {

        @Test
        @DisplayName("should calculate percentage discount correctly")
        void shouldCalculatePercentageDiscountCorrectly() {
            DiscountRule rule = DiscountRule.percentage(20);
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "8折優惠", "描述", rule, startDate, endDate
            );

            Money orderTotal = Money.of(new BigDecimal("1000"));
            Money discount = promotion.calculateDiscount(orderTotal);

            assertEquals(new BigDecimal("200.00"), discount.getAmount());
        }

        @Test
        @DisplayName("should calculate fixed amount discount correctly")
        void shouldCalculateFixedAmountDiscountCorrectly() {
            DiscountRule rule = DiscountRule.fixedAmount(Money.of(new BigDecimal("100")));
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "折100", "描述", rule, startDate, endDate
            );

            Money orderTotal = Money.of(new BigDecimal("1000"));
            Money discount = promotion.calculateDiscount(orderTotal);

            assertEquals(new BigDecimal("100.00"), discount.getAmount());
        }

        @Test
        @DisplayName("should not exceed order total for fixed discount")
        void shouldNotExceedOrderTotalForFixedDiscount() {
            DiscountRule rule = DiscountRule.fixedAmount(Money.of(new BigDecimal("500")));
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "折500", "描述", rule, startDate, endDate
            );

            Money orderTotal = Money.of(new BigDecimal("300"));
            Money discount = promotion.calculateDiscount(orderTotal);

            assertEquals(new BigDecimal("300.00"), discount.getAmount());
        }

        @Test
        @DisplayName("should return zero discount when promotion is not active")
        void shouldReturnZeroDiscountWhenNotActive() {
            DiscountRule rule = DiscountRule.percentage(20);
            Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "未開始", "描述", rule, startDate, endDate
            );

            Money orderTotal = Money.of(new BigDecimal("1000"));
            Money discount = promotion.calculateDiscount(orderTotal);

            assertTrue(discount.getAmount().compareTo(BigDecimal.ZERO) == 0);
        }
    }

    @Nested
    @DisplayName("Minimum Order Amount")
    class MinimumOrderAmount {

        @Test
        @DisplayName("should apply discount when order meets minimum")
        void shouldApplyDiscountWhenOrderMeetsMinimum() {
            DiscountRule rule = DiscountRule.fixedAmountWithMinimum(
                    Money.of(new BigDecimal("100")),
                    Money.of(new BigDecimal("1000"))
            );
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "滿千折百", "描述", rule, startDate, endDate
            );

            Money orderTotal = Money.of(new BigDecimal("1500"));
            Money discount = promotion.calculateDiscount(orderTotal);

            assertEquals(new BigDecimal("100.00"), discount.getAmount());
        }

        @Test
        @DisplayName("should not apply discount when order below minimum")
        void shouldNotApplyDiscountWhenOrderBelowMinimum() {
            DiscountRule rule = DiscountRule.fixedAmountWithMinimum(
                    Money.of(new BigDecimal("100")),
                    Money.of(new BigDecimal("1000"))
            );
            Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Promotion promotion = Promotion.create(
                    "滿千折百", "描述", rule, startDate, endDate
            );

            Money orderTotal = Money.of(new BigDecimal("500"));
            Money discount = promotion.calculateDiscount(orderTotal);

            assertTrue(discount.getAmount().compareTo(BigDecimal.ZERO) == 0);
        }
    }
}
