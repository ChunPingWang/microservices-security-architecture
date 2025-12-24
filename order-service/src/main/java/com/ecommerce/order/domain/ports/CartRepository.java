package com.ecommerce.order.domain.ports;

import com.ecommerce.order.domain.aggregates.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Cart aggregate.
 */
public interface CartRepository {

    /**
     * Saves a cart.
     */
    void save(Cart cart);

    /**
     * Finds a cart by customer ID.
     */
    Optional<Cart> findByCustomerId(UUID customerId);

    /**
     * Deletes a cart by customer ID.
     */
    void deleteByCustomerId(UUID customerId);

    /**
     * Checks if a cart exists for a customer.
     */
    boolean existsByCustomerId(UUID customerId);
}
