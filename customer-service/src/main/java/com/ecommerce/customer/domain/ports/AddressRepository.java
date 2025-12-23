package com.ecommerce.customer.domain.ports;

import com.ecommerce.customer.domain.entities.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Address entity.
 * Defines the contract for address persistence operations.
 */
public interface AddressRepository {

    /**
     * Saves an address.
     *
     * @param address the address to save
     * @return the saved address
     */
    Address save(Address address);

    /**
     * Finds an address by ID.
     *
     * @param id the address ID
     * @return the address if found
     */
    Optional<Address> findById(UUID id);

    /**
     * Finds all addresses for a customer.
     *
     * @param customerId the customer ID
     * @return list of addresses
     */
    List<Address> findByCustomerId(UUID customerId);

    /**
     * Finds the default address for a customer.
     *
     * @param customerId the customer ID
     * @return the default address if found
     */
    Optional<Address> findDefaultByCustomerId(UUID customerId);

    /**
     * Deletes an address by ID.
     *
     * @param id the address ID
     */
    void deleteById(UUID id);

    /**
     * Counts addresses for a customer.
     *
     * @param customerId the customer ID
     * @return the count
     */
    int countByCustomerId(UUID customerId);
}
