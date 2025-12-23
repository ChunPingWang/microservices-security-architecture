package com.ecommerce.product.domain.entities;

import com.ecommerce.product.domain.value_objects.SKU;
import com.ecommerce.shared.domain.value_objects.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Product entity representing a catalog item.
 * Aggregate root for product-related operations.
 */
public class Product {

    private final UUID id;
    private final SKU sku;
    private String name;
    private String description;
    private Money price;
    private UUID categoryId;
    private boolean active;
    private List<String> imageUrls;
    private Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Product(
            UUID id,
            SKU sku,
            String name,
            String description,
            Money price,
            UUID categoryId
    ) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.active = true;
        this.imageUrls = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    /**
     * Factory method to create a new product.
     */
    public static Product create(
            SKU sku,
            String name,
            String description,
            Money price,
            UUID categoryId
    ) {
        Objects.requireNonNull(sku, "SKU is required");
        Objects.requireNonNull(name, "Product name is required");
        Objects.requireNonNull(price, "Price is required");
        Objects.requireNonNull(categoryId, "Category ID is required");

        return new Product(UUID.randomUUID(), sku, name, description, price, categoryId);
    }

    /**
     * Reconstructs a product from persistence.
     */
    public static Product reconstitute(
            UUID id,
            SKU sku,
            String name,
            String description,
            Money price,
            UUID categoryId,
            boolean active,
            List<String> imageUrls,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        Product product = new Product(id, sku, name, description, price, categoryId);
        product.active = active;
        product.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        product.version = version;
        return product;
    }

    /**
     * Updates the product price.
     *
     * @param newPrice the new price
     * @throws IllegalArgumentException if price is negative
     */
    public void updatePrice(Money newPrice) {
        Objects.requireNonNull(newPrice, "Price is required");
        // Money.of() already validates non-negative amount
        this.price = newPrice;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates product name and description.
     *
     * @param newName        the new name
     * @param newDescription the new description (null to keep existing)
     */
    public void updateDetails(String newName, String newDescription) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        this.name = newName;
        if (newDescription != null) {
            this.description = newDescription;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Changes the product category.
     *
     * @param newCategoryId the new category ID
     */
    public void changeCategory(UUID newCategoryId) {
        Objects.requireNonNull(newCategoryId, "Category ID is required");
        this.categoryId = newCategoryId;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivates the product.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Activates the product.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Adds an image URL to the product.
     *
     * @param imageUrl the image URL
     */
    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrls.add(imageUrl);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Removes an image URL from the product.
     *
     * @param imageUrl the image URL to remove
     */
    public void removeImage(String imageUrl) {
        if (this.imageUrls.remove(imageUrl)) {
            this.updatedAt = Instant.now();
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public SKU getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public boolean isActive() {
        return active;
    }

    public List<String> getImageUrls() {
        return Collections.unmodifiableList(imageUrls);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
