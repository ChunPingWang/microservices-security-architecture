package com.ecommerce.customer.infrastructure.persistence.repositories;

import com.ecommerce.customer.infrastructure.persistence.entities.AddressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for AddressJpaEntity.
 */
@Repository
public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, UUID> {

    List<AddressJpaEntity> findByCustomerId(UUID customerId);

    Optional<AddressJpaEntity> findByCustomerIdAndIsDefaultTrue(UUID customerId);

    int countByCustomerId(UUID customerId);
}
