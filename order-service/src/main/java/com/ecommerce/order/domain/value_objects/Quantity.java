package com.ecommerce.order.domain.value_objects;

import java.util.Objects;

/**
 * Value object representing a quantity in cart or order.
 */
public final class Quantity {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 99;

    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY);
        }
        if (value > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_QUANTITY);
        }
        return new Quantity(value);
    }

    public int getValue() {
        return value;
    }

    public Quantity add(int amount) {
        return of(this.value + amount);
    }

    public Quantity subtract(int amount) {
        return of(this.value - amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
