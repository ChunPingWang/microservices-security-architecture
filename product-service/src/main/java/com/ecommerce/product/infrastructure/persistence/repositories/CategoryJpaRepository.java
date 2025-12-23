package com.ecommerce.product.infrastructure.persistence.repositories;

import com.ecommerce.product.infrastructure.persistence.entities.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CategoryJpaEntity.
 */
@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    List<CategoryJpaEntity> findByParentIdIsNullOrderByDisplayOrder();

    List<CategoryJpaEntity> findByParentIdOrderByDisplayOrder(UUID parentId);

    List<CategoryJpaEntity> findByActiveTrueOrderByDisplayOrder();
}
