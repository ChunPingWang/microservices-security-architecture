package com.ecommerce.customer.infrastructure.persistence.adapters;

import com.ecommerce.customer.domain.entities.Address;
import com.ecommerce.customer.domain.ports.AddressRepository;
import com.ecommerce.customer.infrastructure.persistence.entities.AddressJpaEntity;
import com.ecommerce.customer.infrastructure.persistence.mappers.AddressMapper;
import com.ecommerce.customer.infrastructure.persistence.repositories.AddressJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing AddressRepository port using JPA.
 */
@Component
public class AddressRepositoryAdapter implements AddressRepository {

    private final AddressJpaRepository jpaRepository;

    public AddressRepositoryAdapter(AddressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Address save(Address address) {
        AddressJpaEntity jpaEntity = AddressMapper.toJpa(address);
        AddressJpaEntity saved = jpaRepository.save(jpaEntity);
        return AddressMapper.toDomain(saved);
    }

    @Override
    public Optional<Address> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(AddressMapper::toDomain);
    }

    @Override
    public List<Address> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(AddressMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Address> findDefaultByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                .map(AddressMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public int countByCustomerId(UUID customerId) {
        return jpaRepository.countByCustomerId(customerId);
    }
}
