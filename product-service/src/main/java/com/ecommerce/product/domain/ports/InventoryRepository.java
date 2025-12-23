package com.ecommerce.product.domain.ports;

import com.ecommerce.product.domain.entities.Inventory;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Inventory entity.
 */
public interface InventoryRepository {

    /**
     * Saves inventory.
     */
    Inventory save(Inventory inventory);

    /**
     * Finds inventory by product ID.
     */
    Optional<Inventory> findByProductId(UUID productId);

    /**
     * Deletes inventory by product ID.
     */
    void deleteByProductId(UUID productId);
}
