package com.ecommerce.customer.domain.ports;

import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.value_objects.Email;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Customer aggregate.
 * Defines the contract for customer persistence operations.
 */
public interface CustomerRepository {

    /**
     * Saves a customer.
     *
     * @param customer the customer to save
     * @return the saved customer
     */
    Customer save(Customer customer);

    /**
     * Finds a customer by ID.
     *
     * @param id the customer ID
     * @return the customer if found
     */
    Optional<Customer> findById(UUID id);

    /**
     * Finds a customer by email.
     *
     * @param email the email address
     * @return the customer if found
     */
    Optional<Customer> findByEmail(Email email);

    /**
     * Checks if an email is already registered.
     *
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(Email email);

    /**
     * Deletes a customer by ID.
     *
     * @param id the customer ID
     */
    void deleteById(UUID id);
}
