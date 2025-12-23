package com.ecommerce.product.infrastructure.persistence.adapters;

import com.ecommerce.product.domain.entities.Inventory;
import com.ecommerce.product.domain.ports.InventoryRepository;
import com.ecommerce.product.infrastructure.persistence.mappers.InventoryMapper;
import com.ecommerce.product.infrastructure.persistence.repositories.InventoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing InventoryRepository port using JPA.
 */
@Component
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;

    public InventoryRepositoryAdapter(InventoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Inventory save(Inventory inventory) {
        var jpaEntity = InventoryMapper.toJpa(inventory);
        var saved = jpaRepository.save(jpaEntity);
        return InventoryMapper.toDomain(saved);
    }

    @Override
    public Optional<Inventory> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId)
                .map(InventoryMapper::toDomain);
    }

    @Override
    public void deleteByProductId(UUID productId) {
        jpaRepository.deleteByProductId(productId);
    }
}
