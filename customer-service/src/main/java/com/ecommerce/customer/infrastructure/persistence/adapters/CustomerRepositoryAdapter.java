package com.ecommerce.customer.infrastructure.persistence.adapters;

import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.customer.infrastructure.persistence.entities.CustomerJpaEntity;
import com.ecommerce.customer.infrastructure.persistence.mappers.CustomerMapper;
import com.ecommerce.customer.infrastructure.persistence.repositories.CustomerJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing CustomerRepository port using JPA.
 */
@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    public CustomerRepositoryAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity jpaEntity = CustomerMapper.toJpa(customer);
        CustomerJpaEntity saved = jpaRepository.save(jpaEntity);
        return CustomerMapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(CustomerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue())
                .map(CustomerMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
