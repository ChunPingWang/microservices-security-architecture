package com.ecommerce.sales.infrastructure.persistence;

import com.ecommerce.sales.domain.ports.PromotionRepository;
import com.ecommerce.sales.domain.aggregates.Promotion;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of PromotionRepository for development and testing.
 */
@Repository
public class InMemoryPromotionRepository implements PromotionRepository {

    private final Map<UUID, Promotion> promotions = new ConcurrentHashMap<>();

    @Override
    public Promotion save(Promotion promotion) {
        promotions.put(promotion.getId(), promotion);
        return promotion;
    }

    @Override
    public Optional<Promotion> findById(UUID id) {
        return Optional.ofNullable(promotions.get(id));
    }

    @Override
    public List<Promotion> findAll() {
        return new ArrayList<>(promotions.values());
    }

    @Override
    public List<Promotion> findAllActive() {
        return promotions.values().stream()
                .filter(Promotion::isActive)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        promotions.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return promotions.containsKey(id);
    }

    /**
     * Clear all promotions (for testing).
     */
    public void clear() {
        promotions.clear();
    }
}
