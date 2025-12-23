package com.ecommerce.product.domain.value_objects;

import java.util.Objects;

/**
 * Value object representing product stock quantity.
 * Immutable - operations return new instances.
 */
public final class Stock {

    private final int quantity;

    private Stock(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Creates a Stock with the given quantity.
     *
     * @param quantity the stock quantity
     * @return a Stock instance
     * @throws IllegalArgumentException if quantity is negative
     */
    public static Stock of(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        return new Stock(quantity);
    }

    /**
     * Creates a zero stock.
     *
     * @return a Stock with zero quantity
     */
    public static Stock zero() {
        return new Stock(0);
    }

    /**
     * Adds quantity to this stock.
     *
     * @param amount the amount to add
     * @return new Stock with added quantity
     * @throws IllegalArgumentException if amount is not positive
     */
    public Stock add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new Stock(this.quantity + amount);
    }

    /**
     * Subtracts quantity from this stock.
     *
     * @param amount the amount to subtract
     * @return new Stock with subtracted quantity
     * @throws IllegalArgumentException if result would be negative
     */
    public Stock subtract(int amount) {
        if (amount > this.quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock: have " + this.quantity + ", need " + amount
            );
        }
        return new Stock(this.quantity - amount);
    }

    /**
     * Checks if stock is depleted.
     *
     * @return true if quantity is zero
     */
    public boolean isOutOfStock() {
        return quantity == 0;
    }

    /**
     * Checks if stock is below threshold.
     *
     * @param threshold the low stock threshold
     * @return true if quantity is below threshold
     */
    public boolean isLowStock(int threshold) {
        return quantity < threshold;
    }

    /**
     * Checks if there's enough stock for the requested amount.
     *
     * @param amount the requested amount
     * @return true if stock is sufficient
     */
    public boolean hasEnough(int amount) {
        return quantity >= amount;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Stock stock = (Stock) o;
        return quantity == stock.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity);
    }

    @Override
    public String toString() {
        return String.valueOf(quantity);
    }
}
