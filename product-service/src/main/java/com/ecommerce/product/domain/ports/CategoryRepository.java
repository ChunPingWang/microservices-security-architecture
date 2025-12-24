package com.ecommerce.product.domain.ports;

import com.ecommerce.product.domain.entities.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Category entity.
 */
public interface CategoryRepository {

    /**
     * Saves a category.
     */
    Category save(Category category);

    /**
     * Finds a category by ID.
     */
    Optional<Category> findById(UUID id);

    /**
     * Finds all root categories.
     */
    List<Category> findAllRoots();

    /**
     * Finds all child categories of a parent.
     */
    List<Category> findByParentId(UUID parentId);

    /**
     * Finds all active categories.
     */
    List<Category> findAllActive();

    /**
     * Deletes a category by ID.
     */
    void deleteById(UUID id);
}
