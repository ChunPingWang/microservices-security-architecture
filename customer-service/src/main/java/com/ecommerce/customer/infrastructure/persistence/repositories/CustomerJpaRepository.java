package com.ecommerce.customer.infrastructure.persistence.repositories;

import com.ecommerce.customer.infrastructure.persistence.entities.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CustomerJpaEntity.
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
