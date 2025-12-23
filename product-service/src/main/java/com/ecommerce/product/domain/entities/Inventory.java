package com.ecommerce.product.domain.entities;

import com.ecommerce.product.domain.events.LowStockDetected;
import com.ecommerce.product.domain.events.StockDepleted;
import com.ecommerce.product.domain.value_objects.Stock;
import com.ecommerce.shared.domain.events.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Inventory entity managing product stock levels.
 */
public class Inventory {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private final UUID id;
    private final UUID productId;
    private Stock stock;
    private int lowStockThreshold;
    private int reservedQuantity;
    private Instant lastRestockedAt;
    private Instant updatedAt;
    private long version;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Inventory(UUID id, UUID productId, Stock stock) {
        this.id = id;
        this.productId = productId;
        this.stock = stock;
        this.lowStockThreshold = DEFAULT_LOW_STOCK_THRESHOLD;
        this.reservedQuantity = 0;
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    /**
     * Creates a new inventory record for a product.
     */
    public static Inventory create(UUID productId, int initialStock) {
        Objects.requireNonNull(productId, "Product ID is required");
        return new Inventory(UUID.randomUUID(), productId, Stock.of(initialStock));
    }

    /**
     * Reconstructs inventory from persistence.
     */
    public static Inventory reconstitute(
            UUID id,
            UUID productId,
            Stock stock,
            int lowStockThreshold,
            int reservedQuantity,
            Instant lastRestockedAt,
            Instant updatedAt,
            long version
    ) {
        Inventory inventory = new Inventory(id, productId, stock);
        inventory.lowStockThreshold = lowStockThreshold;
        inventory.reservedQuantity = reservedQuantity;
        inventory.lastRestockedAt = lastRestockedAt;
        inventory.updatedAt = updatedAt;
        inventory.version = version;
        return inventory;
    }

    /**
     * Adds stock (restock operation).
     *
     * @param quantity the quantity to add
     */
    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        this.stock = this.stock.add(quantity);
        this.lastRestockedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Reserves stock for an order.
     *
     * @param quantity the quantity to reserve
     * @throws IllegalStateException if insufficient available stock
     */
    public void reserve(int quantity) {
        if (!hasAvailableStock(quantity)) {
            throw new IllegalStateException(
                    "Insufficient available stock: have " + getAvailableQuantity() +
                            ", need " + quantity
            );
        }
        this.reservedQuantity += quantity;
        this.updatedAt = Instant.now();

        checkAndRaiseLowStockEvent();
    }

    /**
     * Releases reserved stock.
     *
     * @param quantity the quantity to release
     */
    public void releaseReservation(int quantity) {
        if (quantity > this.reservedQuantity) {
            this.reservedQuantity = 0;
        } else {
            this.reservedQuantity -= quantity;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Confirms a reservation by reducing actual stock.
     *
     * @param quantity the quantity to confirm
     */
    public void confirmReservation(int quantity) {
        if (quantity > this.reservedQuantity) {
            throw new IllegalStateException("Cannot confirm more than reserved");
        }
        this.stock = this.stock.subtract(quantity);
        this.reservedQuantity -= quantity;
        this.updatedAt = Instant.now();

        if (this.stock.isOutOfStock()) {
            domainEvents.add(new StockDepleted(productId.toString()));
        }
    }

    /**
     * Directly reduces stock without reservation.
     *
     * @param quantity the quantity to reduce
     */
    public void reduceStock(int quantity) {
        this.stock = this.stock.subtract(quantity);
        this.updatedAt = Instant.now();

        checkAndRaiseLowStockEvent();

        if (this.stock.isOutOfStock()) {
            domainEvents.add(new StockDepleted(productId.toString()));
        }
    }

    /**
     * Sets the low stock threshold.
     *
     * @param threshold the new threshold
     */
    public void setLowStockThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        this.lowStockThreshold = threshold;
        this.updatedAt = Instant.now();
    }

    private void checkAndRaiseLowStockEvent() {
        if (stock.isLowStock(lowStockThreshold)) {
            domainEvents.add(new LowStockDetected(
                    productId.toString(),
                    stock.getQuantity(),
                    lowStockThreshold
            ));
        }
    }

    /**
     * Checks if there's enough available stock.
     */
    public boolean hasAvailableStock(int quantity) {
        return getAvailableQuantity() >= quantity;
    }

    /**
     * Gets the available (non-reserved) quantity.
     */
    public int getAvailableQuantity() {
        return stock.getQuantity() - reservedQuantity;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public Stock getStock() {
        return stock;
    }

    public int getTotalQuantity() {
        return stock.getQuantity();
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public boolean isLowStock() {
        return stock.isLowStock(lowStockThreshold);
    }

    public boolean isOutOfStock() {
        return stock.isOutOfStock();
    }

    public Instant getLastRestockedAt() {
        return lastRestockedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
