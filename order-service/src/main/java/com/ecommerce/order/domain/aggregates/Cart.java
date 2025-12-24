package com.ecommerce.order.domain.aggregates;

import com.ecommerce.order.domain.entities.CartItem;
import com.ecommerce.shared.domain.value_objects.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Aggregate root for shopping cart.
 * A cart belongs to a customer and contains multiple items.
 */
public class Cart {

    private static final int MAX_ITEMS = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 99;

    private final UUID id;
    private final UUID customerId;
    private final Map<UUID, CartItem> items; // productId -> CartItem
    private Instant createdAt;
    private Instant updatedAt;

    private Cart(UUID id, UUID customerId) {
        this.id = id;
        this.customerId = customerId;
        this.items = new LinkedHashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Cart create(UUID customerId) {
        Objects.requireNonNull(customerId, "Customer ID is required");
        return new Cart(UUID.randomUUID(), customerId);
    }

    /**
     * Reconstitutes a Cart from persistence.
     */
    public static Cart reconstitute(
            UUID id,
            UUID customerId,
            List<CartItem> cartItems,
            Instant createdAt,
            Instant updatedAt
    ) {
        Cart cart = new Cart(id, customerId);
        cart.createdAt = createdAt;
        cart.updatedAt = updatedAt;
        for (CartItem item : cartItems) {
            cart.items.put(item.getProductId(), item);
        }
        return cart;
    }

    /**
     * Adds a product to the cart.
     * If the product already exists, increases the quantity.
     */
    public CartItem addItem(
            UUID productId,
            String productName,
            String productSku,
            Money unitPrice,
            int quantity
    ) {
        validateCanAddItem(productId, quantity);

        if (items.containsKey(productId)) {
            CartItem existingItem = items.get(productId);
            existingItem.increaseQuantity(quantity);
            this.updatedAt = Instant.now();
            return existingItem;
        }

        CartItem newItem = CartItem.create(productId, productName, productSku, unitPrice, quantity);
        items.put(productId, newItem);
        this.updatedAt = Instant.now();
        return newItem;
    }

    private void validateCanAddItem(UUID productId, int quantity) {
        if (!items.containsKey(productId) && items.size() >= MAX_ITEMS) {
            throw new IllegalStateException("Cart cannot have more than " + MAX_ITEMS + " different items");
        }

        if (items.containsKey(productId)) {
            int newQuantity = items.get(productId).getQuantityValue() + quantity;
            if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                throw new IllegalArgumentException(
                        "Cannot add more items. Maximum quantity per item is " + MAX_QUANTITY_PER_ITEM
                );
            }
        }
    }

    /**
     * Updates the quantity of an item in the cart.
     */
    public void updateItemQuantity(UUID productId, int newQuantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product not found in cart: " + productId);
        }
        item.updateQuantity(newQuantity);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes an item from the cart.
     */
    public void removeItem(UUID productId) {
        CartItem removed = items.remove(productId);
        if (removed == null) {
            throw new IllegalArgumentException("Product not found in cart: " + productId);
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Clears all items from the cart.
     */
    public void clear() {
        items.clear();
        this.updatedAt = Instant.now();
    }

    /**
     * Calculates the total price of all items in the cart.
     */
    public Money getTotal() {
        return items.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(Money.zero(), Money::add);
    }

    /**
     * Gets the total number of items (sum of quantities).
     */
    public int getTotalItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantityValue)
                .sum();
    }

    /**
     * Checks if the cart is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Gets an item by product ID.
     */
    public Optional<CartItem> getItem(UUID productId) {
        return Optional.ofNullable(items.get(productId));
    }

    /**
     * Checks if the cart contains a product.
     */
    public boolean containsProduct(UUID productId) {
        return items.containsKey(productId);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public int getItemCount() {
        return items.size();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
