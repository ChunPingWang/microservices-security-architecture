package com.ecommerce.sales.unit.domain;

import com.ecommerce.sales.domain.entities.Coupon;
import com.ecommerce.sales.domain.value_objects.CouponCode;
import com.ecommerce.sales.domain.value_objects.DiscountRule;
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
 * Unit tests for Coupon entity.
 */
@DisplayName("Coupon Entity")
class CouponTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Coupon Creation")
    class CouponCreation {

        @Test
        @DisplayName("should create coupon with valid data")
        void shouldCreateCouponWithValidData() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(
                    code,
                    "新會員折扣",
                    rule,
                    expiryDate,
                    100  // max uses
            );

            assertNotNull(coupon.getId());
            assertEquals(code, coupon.getCode());
            assertEquals("新會員折扣", coupon.getDescription());
            assertEquals(rule, coupon.getDiscountRule());
            assertEquals(0, coupon.getUsageCount());
            assertEquals(100, coupon.getMaxUses());
            assertTrue(coupon.isValid());
        }

        @Test
        @DisplayName("should create single-use coupon")
        void shouldCreateSingleUseCoupon() {
            CouponCode code = CouponCode.of("SINGLE2024");
            DiscountRule rule = DiscountRule.fixedAmount(Money.of(new BigDecimal("50")));
            Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);

            Coupon coupon = Coupon.createSingleUse(code, "單次使用券", rule, expiryDate);

            assertEquals(1, coupon.getMaxUses());
            assertTrue(coupon.isSingleUse());
        }

        @Test
        @DisplayName("should create unlimited use coupon")
        void shouldCreateUnlimitedUseCoupon() {
            CouponCode code = CouponCode.of("UNLIMITED");
            DiscountRule rule = DiscountRule.percentage(5);
            Instant expiryDate = Instant.now().plus(365, ChronoUnit.DAYS);

            Coupon coupon = Coupon.createUnlimited(code, "無限次券", rule, expiryDate);

            assertNull(coupon.getMaxUses());
            assertFalse(coupon.isSingleUse());
        }

        @Test
        @DisplayName("should fail when code is null")
        void shouldFailWhenCodeIsNull() {
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            assertThrows(NullPointerException.class, () ->
                    Coupon.create(null, "描述", rule, expiryDate, 100)
            );
        }
    }

    @Nested
    @DisplayName("Coupon Validation")
    class CouponValidation {

        @Test
        @DisplayName("should be valid when not expired and has uses remaining")
        void shouldBeValidWhenNotExpiredAndHasUsesRemaining() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "有效券", rule, expiryDate, 10);

            assertTrue(coupon.isValid());
            assertTrue(coupon.canBeUsed());
        }

        @Test
        @DisplayName("should not be valid when expired")
        void shouldNotBeValidWhenExpired() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().minus(1, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "過期券", rule, expiryDate, 10);

            assertFalse(coupon.isValid());
            assertFalse(coupon.canBeUsed());
            assertTrue(coupon.isExpired());
        }

        @Test
        @DisplayName("should not be valid when max uses reached")
        void shouldNotBeValidWhenMaxUsesReached() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "用完券", rule, expiryDate, 1);
            coupon.use(CUSTOMER_ID);

            assertFalse(coupon.canBeUsed());
            assertTrue(coupon.isExhausted());
        }

        @Test
        @DisplayName("should not be valid when deactivated")
        void shouldNotBeValidWhenDeactivated() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "停用券", rule, expiryDate, 10);
            coupon.deactivate();

            assertFalse(coupon.isValid());
            assertFalse(coupon.canBeUsed());
        }
    }

    @Nested
    @DisplayName("Coupon Usage")
    class CouponUsage {

        @Test
        @DisplayName("should increment usage count when used")
        void shouldIncrementUsageCountWhenUsed() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "可用券", rule, expiryDate, 10);

            assertEquals(0, coupon.getUsageCount());
            coupon.use(CUSTOMER_ID);
            assertEquals(1, coupon.getUsageCount());
            coupon.use(UUID.randomUUID());
            assertEquals(2, coupon.getUsageCount());
        }

        @Test
        @DisplayName("should fail to use when expired")
        void shouldFailToUseWhenExpired() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().minus(1, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "過期券", rule, expiryDate, 10);

            assertThrows(IllegalStateException.class, () -> coupon.use(CUSTOMER_ID));
        }

        @Test
        @DisplayName("should fail to use when max uses reached")
        void shouldFailToUseWhenMaxUsesReached() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "單次券", rule, expiryDate, 1);
            coupon.use(CUSTOMER_ID);

            assertThrows(IllegalStateException.class, () -> coupon.use(UUID.randomUUID()));
        }

        @Test
        @DisplayName("should track which customers used the coupon")
        void shouldTrackWhichCustomersUsedCoupon() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "追蹤券", rule, expiryDate, 10);
            UUID customer1 = UUID.randomUUID();
            UUID customer2 = UUID.randomUUID();

            coupon.use(customer1);
            coupon.use(customer2);

            assertTrue(coupon.hasBeenUsedBy(customer1));
            assertTrue(coupon.hasBeenUsedBy(customer2));
            assertFalse(coupon.hasBeenUsedBy(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("Per Customer Limit")
    class PerCustomerLimit {

        @Test
        @DisplayName("should enforce per customer usage limit")
        void shouldEnforcePerCustomerUsageLimit() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.createWithPerCustomerLimit(
                    code, "每人限用一次", rule, expiryDate, 100, 1
            );

            coupon.use(CUSTOMER_ID);

            assertThrows(IllegalStateException.class, () -> coupon.use(CUSTOMER_ID));
        }

        @Test
        @DisplayName("should allow different customers to use")
        void shouldAllowDifferentCustomersToUse() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(10);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.createWithPerCustomerLimit(
                    code, "每人限用一次", rule, expiryDate, 100, 1
            );

            UUID customer1 = UUID.randomUUID();
            UUID customer2 = UUID.randomUUID();

            coupon.use(customer1);
            coupon.use(customer2);

            assertEquals(2, coupon.getUsageCount());
        }
    }

    @Nested
    @DisplayName("Minimum Order Requirement")
    class MinimumOrderRequirement {

        @Test
        @DisplayName("should validate minimum order amount")
        void shouldValidateMinimumOrderAmount() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.fixedAmountWithMinimum(
                    Money.of(new BigDecimal("100")),
                    Money.of(new BigDecimal("1000"))
            );
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "滿千折百", rule, expiryDate, 10);

            assertTrue(coupon.isApplicableTo(Money.of(new BigDecimal("1500"))));
            assertFalse(coupon.isApplicableTo(Money.of(new BigDecimal("500"))));
        }

        @Test
        @DisplayName("should calculate discount correctly")
        void shouldCalculateDiscountCorrectly() {
            CouponCode code = CouponCode.generate();
            DiscountRule rule = DiscountRule.percentage(20);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

            Coupon coupon = Coupon.create(code, "8折券", rule, expiryDate, 10);
            Money orderTotal = Money.of(new BigDecimal("1000"));

            Money discount = coupon.calculateDiscount(orderTotal);

            assertEquals(new BigDecimal("200.00"), discount.getAmount());
        }
    }
}
