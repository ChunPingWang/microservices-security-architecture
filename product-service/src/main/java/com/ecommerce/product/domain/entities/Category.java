package com.ecommerce.product.domain.entities;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Category entity for product classification.
 */
public class Category {

    private final UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private int displayOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Category(UUID id, String name, String description, UUID parentId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.displayOrder = 0;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Creates a new category.
     */
    public static Category create(String name, String description, UUID parentId) {
        Objects.requireNonNull(name, "Category name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        return new Category(UUID.randomUUID(), name, description, parentId);
    }

    /**
     * Creates a root category (no parent).
     */
    public static Category createRoot(String name, String description) {
        return create(name, description, null);
    }

    /**
     * Reconstructs a category from persistence.
     */
    public static Category reconstitute(
            UUID id,
            String name,
            String description,
            UUID parentId,
            int displayOrder,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        Category category = new Category(id, name, description, parentId);
        category.displayOrder = displayOrder;
        category.active = active;
        category.createdAt = createdAt;
        category.updatedAt = updatedAt;
        return category;
    }

    /**
     * Updates the category details.
     */
    public void update(String newName, String newDescription) {
        if (newName != null && !newName.isBlank()) {
            this.name = newName;
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Changes the parent category.
     */
    public void changeParent(UUID newParentId) {
        this.parentId = newParentId;
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the display order.
     */
    public void setDisplayOrder(int order) {
        this.displayOrder = order;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivates the category.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Activates the category.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if this is a root category.
     */
    public boolean isRoot() {
        return parentId == null;
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

    public UUID getParentId() {
        return parentId;
    }

    public int getDisplayOrder() {
        return displayOrder;
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
}
