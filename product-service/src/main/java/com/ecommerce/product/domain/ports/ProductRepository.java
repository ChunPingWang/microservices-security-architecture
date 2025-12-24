package com.ecommerce.product.domain.ports;

import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.value_objects.SKU;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Product aggregate.
 */
public interface ProductRepository {

    /**
     * Saves a product.
     */
    Product save(Product product);

    /**
     * Finds a product by ID.
     */
    Optional<Product> findById(UUID id);

    /**
     * Finds a product by SKU.
     */
    Optional<Product> findBySku(SKU sku);

    /**
     * Finds all active products in a category.
     */
    List<Product> findByCategory(UUID categoryId);

    /**
     * Finds all active products with pagination.
     */
    List<Product> findAllActive(int page, int size);

    /**
     * Counts all active products.
     */
    long countActive();

    /**
     * Checks if a SKU already exists.
     */
    boolean existsBySku(SKU sku);

    /**
     * Deletes a product by ID.
     */
    void deleteById(UUID id);
}
