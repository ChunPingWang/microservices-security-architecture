package com.ecommerce.sales.domain.entities;

import com.ecommerce.sales.domain.value_objects.CouponCode;
import com.ecommerce.sales.domain.value_objects.DiscountRule;
import com.ecommerce.shared.domain.value_objects.Money;

import java.time.Instant;
import java.util.*;

/**
 * Coupon entity.
 * Represents a discount coupon that can be used by customers.
 */
public class Coupon {

    private final UUID id;
    private final CouponCode code;
    private final String description;
    private final DiscountRule discountRule;
    private final Instant expiryDate;
    private final Integer maxUses;
    private final Integer maxUsesPerCustomer;
    private int usageCount;
    private boolean active;
    private final Map<UUID, Integer> customerUsage;
    private final Instant createdAt;
    private Instant updatedAt;

    private Coupon(UUID id, CouponCode code, String description, DiscountRule discountRule,
                   Instant expiryDate, Integer maxUses, Integer maxUsesPerCustomer, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountRule = discountRule;
        this.expiryDate = expiryDate;
        this.maxUses = maxUses;
        this.maxUsesPerCustomer = maxUsesPerCustomer;
        this.usageCount = 0;
        this.active = true;
        this.customerUsage = new HashMap<>();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Creates a new coupon with limited uses.
     */
    public static Coupon create(CouponCode code, String description, DiscountRule discountRule,
                                 Instant expiryDate, int maxUses) {
        Objects.requireNonNull(code, "Coupon code must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(discountRule, "Discount rule must not be null");
        Objects.requireNonNull(expiryDate, "Expiry date must not be null");

        return new Coupon(
                UUID.randomUUID(),
                code,
                description,
                discountRule,
                expiryDate,
                maxUses,
                null,
                Instant.now()
        );
    }

    /**
     * Creates a single-use coupon.
     */
    public static Coupon createSingleUse(CouponCode code, String description,
                                          DiscountRule discountRule, Instant expiryDate) {
        return create(code, description, discountRule, expiryDate, 1);
    }

    /**
     * Creates an unlimited use coupon.
     */
    public static Coupon createUnlimited(CouponCode code, String description,
                                          DiscountRule discountRule, Instant expiryDate) {
        Objects.requireNonNull(code, "Coupon code must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(discountRule, "Discount rule must not be null");
        Objects.requireNonNull(expiryDate, "Expiry date must not be null");

        return new Coupon(
                UUID.randomUUID(),
                code,
                description,
                discountRule,
                expiryDate,
                null,
                null,
                Instant.now()
        );
    }

    /**
     * Creates a coupon with per-customer limit.
     */
    public static Coupon createWithPerCustomerLimit(CouponCode code, String description,
                                                     DiscountRule discountRule, Instant expiryDate,
                                                     int maxUses, int maxUsesPerCustomer) {
        Objects.requireNonNull(code, "Coupon code must not be null");
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(discountRule, "Discount rule must not be null");
        Objects.requireNonNull(expiryDate, "Expiry date must not be null");

        return new Coupon(
                UUID.randomUUID(),
                code,
                description,
                discountRule,
                expiryDate,
                maxUses,
                maxUsesPerCustomer,
                Instant.now()
        );
    }

    /**
     * Reconstitutes a coupon from persistence.
     */
    public static Coupon reconstitute(UUID id, CouponCode code, String description,
                                       DiscountRule discountRule, Instant expiryDate,
                                       Integer maxUses, Integer maxUsesPerCustomer,
                                       int usageCount, boolean active,
                                       Map<UUID, Integer> customerUsage,
                                       Instant createdAt, Instant updatedAt) {
        Coupon coupon = new Coupon(id, code, description, discountRule,
                expiryDate, maxUses, maxUsesPerCustomer, createdAt);
        coupon.usageCount = usageCount;
        coupon.active = active;
        coupon.customerUsage.putAll(customerUsage);
        coupon.updatedAt = updatedAt;
        return coupon;
    }

    /**
     * Checks if the coupon is still valid (not expired and not deactivated).
     */
    public boolean isValid() {
        return active && !isExpired();
    }

    /**
     * Checks if the coupon can be used (valid and has uses remaining).
     */
    public boolean canBeUsed() {
        return isValid() && !isExhausted();
    }

    /**
     * Checks if the coupon can be used by a specific customer.
     */
    public boolean canBeUsedBy(UUID customerId) {
        if (!canBeUsed()) {
            return false;
        }
        if (maxUsesPerCustomer == null) {
            return true;
        }
        int customerUseCount = customerUsage.getOrDefault(customerId, 0);
        return customerUseCount < maxUsesPerCustomer;
    }

    /**
     * Checks if the coupon has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    /**
     * Checks if the coupon has reached its max uses.
     */
    public boolean isExhausted() {
        return maxUses != null && usageCount >= maxUses;
    }

    /**
     * Checks if this is a single-use coupon.
     */
    public boolean isSingleUse() {
        return maxUses != null && maxUses == 1;
    }

    /**
     * Uses the coupon for a customer.
     */
    public void use(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer ID must not be null");

        if (isExpired()) {
            throw new IllegalStateException("Coupon has expired");
        }

        if (isExhausted()) {
            throw new IllegalStateException("Coupon has reached maximum uses");
        }

        if (maxUsesPerCustomer != null) {
            int customerUseCount = customerUsage.getOrDefault(customerId, 0);
            if (customerUseCount >= maxUsesPerCustomer) {
                throw new IllegalStateException("Customer has reached maximum uses for this coupon");
            }
        }

        this.usageCount++;
        this.customerUsage.merge(customerId, 1, Integer::sum);
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the coupon has been used by a specific customer.
     */
    public boolean hasBeenUsedBy(UUID customerId) {
        return customerUsage.containsKey(customerId);
    }

    /**
     * Deactivates the coupon.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Reactivates the coupon.
     */
    public void reactivate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the coupon is applicable to an order with given total.
     */
    public boolean isApplicableTo(Money orderTotal) {
        return discountRule.meetsMinimum(orderTotal);
    }

    /**
     * Calculates the discount amount for a given order total.
     */
    public Money calculateDiscount(Money orderTotal) {
        if (!isApplicableTo(orderTotal)) {
            return Money.zero();
        }
        return discountRule.calculateDiscount(orderTotal);
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public CouponCode getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountRule getDiscountRule() {
        return discountRule;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public Integer getMaxUsesPerCustomer() {
        return maxUsesPerCustomer;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return id.equals(coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code=" + code +
                ", usageCount=" + usageCount +
                ", active=" + active +
                '}';
    }
}
