package com.ecommerce.order.domain.entities;

import com.ecommerce.order.domain.value_objects.Quantity;
import com.ecommerce.shared.domain.value_objects.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an item in a shopping cart.
 */
public class CartItem {

    private final UUID id;
    private final UUID productId;
    private final String productName;
    private final String productSku;
    private final Money unitPrice;
    private Quantity quantity;
    private final Instant addedAt;
    private Instant updatedAt;

    private CartItem(
            UUID id,
            UUID productId,
            String productName,
            String productSku,
            Money unitPrice,
            Quantity quantity,
            Instant addedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.addedAt = addedAt;
        this.updatedAt = addedAt;
    }

    public static CartItem create(
            UUID productId,
            String productName,
            String productSku,
            Money unitPrice,
            int quantity
    ) {
        Objects.requireNonNull(productId, "Product ID is required");
        Objects.requireNonNull(productName, "Product name is required");
        Objects.requireNonNull(productSku, "Product SKU is required");
        Objects.requireNonNull(unitPrice, "Unit price is required");

        return new CartItem(
                UUID.randomUUID(),
                productId,
                productName,
                productSku,
                unitPrice,
                Quantity.of(quantity),
                Instant.now()
        );
    }

    /**
     * Reconstitutes a CartItem from persistence.
     */
    public static CartItem reconstitute(
            UUID id,
            UUID productId,
            String productName,
            String productSku,
            Money unitPrice,
            int quantity,
            Instant addedAt,
            Instant updatedAt
    ) {
        CartItem item = new CartItem(id, productId, productName, productSku, unitPrice, Quantity.of(quantity), addedAt);
        item.updatedAt = updatedAt;
        return item;
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = Quantity.of(newQuantity);
        this.updatedAt = Instant.now();
    }

    public void increaseQuantity(int amount) {
        this.quantity = quantity.add(amount);
        this.updatedAt = Instant.now();
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity.getValue());
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public int getQuantityValue() {
        return quantity.getValue();
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
