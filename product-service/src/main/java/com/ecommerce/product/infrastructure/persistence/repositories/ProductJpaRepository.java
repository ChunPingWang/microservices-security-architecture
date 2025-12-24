package com.ecommerce.product.infrastructure.persistence.repositories;

import com.ecommerce.product.infrastructure.persistence.entities.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProductJpaEntity.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findBySku(String sku);

    List<ProductJpaEntity> findByCategoryIdAndActiveTrue(UUID categoryId);

    Page<ProductJpaEntity> findByActiveTrue(Pageable pageable);

    long countByActiveTrue();

    boolean existsBySku(String sku);
}
