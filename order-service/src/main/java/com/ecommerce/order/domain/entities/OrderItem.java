package com.ecommerce.order.domain.entities;

import com.ecommerce.shared.domain.value_objects.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an item in an order.
 * Immutable snapshot of product at time of order.
 */
public class OrderItem {

    private final UUID id;
    private final UUID productId;
    private final String productName;
    private final String productSku;
    private final Money unitPrice;
    private final int quantity;
    private final Money subtotal;
    private final Instant createdAt;

    private OrderItem(UUID id, UUID productId, String productName, String productSku,
                      Money unitPrice, int quantity, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(quantity);
        this.createdAt = createdAt;
    }

    /**
     * Creates a new order item.
     */
    public static OrderItem create(UUID productId, String productName, String productSku,
                                   Money unitPrice, int quantity) {
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(productName, "Product name must not be null");
        Objects.requireNonNull(productSku, "Product SKU must not be null");
        Objects.requireNonNull(unitPrice, "Unit price must not be null");

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        return new OrderItem(
                UUID.randomUUID(),
                productId,
                productName,
                productSku,
                unitPrice,
                quantity,
                Instant.now()
        );
    }

    /**
     * Reconstitutes an order item from persistence.
     */
    public static OrderItem reconstitute(UUID id, UUID productId, String productName,
                                          String productSku, Money unitPrice, int quantity,
                                          Instant createdAt) {
        return new OrderItem(id, productId, productName, productSku, unitPrice, quantity, createdAt);
    }

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

    public int getQuantity() {
        return quantity;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id.equals(orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
