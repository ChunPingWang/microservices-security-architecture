package com.ecommerce.product.infrastructure.persistence.repositories;

import com.ecommerce.product.infrastructure.persistence.entities.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for InventoryJpaEntity.
 */
@Repository
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, UUID> {

    Optional<InventoryJpaEntity> findByProductId(UUID productId);

    void deleteByProductId(UUID productId);
}
