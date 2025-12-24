package com.ecommerce.sales.domain.aggregates;

import com.ecommerce.sales.domain.value_objects.DiscountRule;
import com.ecommerce.shared.domain.value_objects.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Promotion aggregate root.
 * Represents a promotional offer with time-based validity.
 */
public class Promotion {

    private final UUID id;
    private String name;
    private String description;
    private final DiscountRule discountRule;
    private final Instant startDate;
    private final Instant endDate;
    private boolean manuallyDeactivated;
    private final Instant createdAt;
    private Instant updatedAt;

    private Promotion(UUID id, String name, String description, DiscountRule discountRule,
                      Instant startDate, Instant endDate, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.discountRule = discountRule;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manuallyDeactivated = false;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Creates a new promotion.
     */
    public static Promotion create(String name, String description, DiscountRule discountRule,
                                    Instant startDate, Instant endDate) {
        Objects.requireNonNull(name, "Promotion name must not be null");
        Objects.requireNonNull(description, "Promotion description must not be null");
        Objects.requireNonNull(discountRule, "Discount rule must not be null");
        Objects.requireNonNull(startDate, "Start date must not be null");
        Objects.requireNonNull(endDate, "End date must not be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        return new Promotion(
                UUID.randomUUID(),
                name,
                description,
                discountRule,
                startDate,
                endDate,
                Instant.now()
        );
    }

    /**
     * Reconstitutes a promotion from persistence.
     */
    public static Promotion reconstitute(UUID id, String name, String description,
                                          DiscountRule discountRule, Instant startDate,
                                          Instant endDate, boolean manuallyDeactivated,
                                          Instant createdAt, Instant updatedAt) {
        Promotion promotion = new Promotion(id, name, description, discountRule,
                startDate, endDate, createdAt);
        promotion.manuallyDeactivated = manuallyDeactivated;
        promotion.updatedAt = updatedAt;
        return promotion;
    }

    /**
     * Checks if the promotion is currently active.
     */
    public boolean isActive() {
        if (manuallyDeactivated) {
            return false;
        }
        Instant now = Instant.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /**
     * Checks if the promotion has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(endDate);
    }

    /**
     * Checks if the promotion has been manually deactivated.
     */
    public boolean isManuallyDeactivated() {
        return manuallyDeactivated;
    }

    /**
     * Deactivates the promotion manually.
     */
    public void deactivate() {
        this.manuallyDeactivated = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Reactivates the promotion.
     */
    public void activate() {
        this.manuallyDeactivated = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the promotion details.
     */
    public void update(String name, String description) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.updatedAt = Instant.now();
    }

    /**
     * Calculates the discount amount for a given order total.
     */
    public Money calculateDiscount(Money orderTotal) {
        if (!isActive()) {
            return Money.zero();
        }
        return discountRule.calculateDiscount(orderTotal);
    }

    /**
     * Checks if the order meets the minimum requirement.
     */
    public boolean isApplicableTo(Money orderTotal) {
        return isActive() && discountRule.meetsMinimum(orderTotal);
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DiscountRule getDiscountRule() {
        return discountRule;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
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
        Promotion promotion = (Promotion) o;
        return id.equals(promotion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + isActive() +
                '}';
    }
}
