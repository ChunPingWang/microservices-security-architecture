package com.ecommerce.order.infrastructure.persistence.adapters;

import com.ecommerce.order.domain.aggregates.Cart;
import com.ecommerce.order.domain.ports.CartRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of CartRepository.
 * For production, replace with Redis-backed implementation.
 */
@Repository
public class InMemoryCartRepository implements CartRepository {

    private final Map<UUID, Cart> carts = new ConcurrentHashMap<>();

    @Override
    public void save(Cart cart) {
        carts.put(cart.getCustomerId(), cart);
    }

    @Override
    public Optional<Cart> findByCustomerId(UUID customerId) {
        return Optional.ofNullable(carts.get(customerId));
    }

    @Override
    public void deleteByCustomerId(UUID customerId) {
        carts.remove(customerId);
    }

    @Override
    public boolean existsByCustomerId(UUID customerId) {
        return carts.containsKey(customerId);
    }
}
