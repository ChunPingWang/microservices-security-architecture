package com.ecommerce.sales.infrastructure.persistence;

import com.ecommerce.sales.domain.ports.CouponRepository;
import com.ecommerce.sales.domain.entities.Coupon;
import com.ecommerce.sales.domain.value_objects.CouponCode;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of CouponRepository for development and testing.
 */
@Repository
public class InMemoryCouponRepository implements CouponRepository {

    private final Map<UUID, Coupon> coupons = new ConcurrentHashMap<>();

    @Override
    public Coupon save(Coupon coupon) {
        coupons.put(coupon.getId(), coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return Optional.ofNullable(coupons.get(id));
    }

    @Override
    public Optional<Coupon> findByCode(CouponCode code) {
        return coupons.values().stream()
                .filter(coupon -> coupon.getCode().equals(code))
                .findFirst();
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return coupons.values().stream()
                .filter(coupon -> coupon.getCode().getValue().equals(code))
                .findFirst();
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(coupons.values());
    }

    @Override
    public List<Coupon> findAllValid() {
        return coupons.values().stream()
                .filter(Coupon::isValid)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        coupons.remove(id);
    }

    @Override
    public boolean existsByCode(CouponCode code) {
        return coupons.values().stream()
                .anyMatch(coupon -> coupon.getCode().equals(code));
    }

    /**
     * Clear all coupons (for testing).
     */
    public void clear() {
        coupons.clear();
    }
}
