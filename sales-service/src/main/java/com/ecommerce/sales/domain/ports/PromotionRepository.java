package com.ecommerce.sales.domain.ports;

import com.ecommerce.sales.domain.aggregates.Promotion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Promotion aggregate.
 */
public interface PromotionRepository {

    /**
     * Saves a promotion.
     */
    Promotion save(Promotion promotion);

    /**
     * Finds a promotion by ID.
     */
    Optional<Promotion> findById(UUID promotionId);

    /**
     * Finds all active promotions.
     */
    List<Promotion> findAllActive();

    /**
     * Finds all promotions.
     */
    List<Promotion> findAll();

    /**
     * Deletes a promotion.
     */
    void delete(UUID promotionId);

    /**
     * Checks if a promotion exists.
     */
    boolean existsById(UUID promotionId);
}
