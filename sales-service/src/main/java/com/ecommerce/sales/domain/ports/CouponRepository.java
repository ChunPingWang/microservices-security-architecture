package com.ecommerce.sales.domain.ports;

import com.ecommerce.sales.domain.entities.Coupon;
import com.ecommerce.sales.domain.value_objects.CouponCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Coupon entity.
 */
public interface CouponRepository {

    /**
     * Saves a coupon.
     */
    Coupon save(Coupon coupon);

    /**
     * Finds a coupon by ID.
     */
    Optional<Coupon> findById(UUID couponId);

    /**
     * Finds a coupon by code.
     */
    Optional<Coupon> findByCode(CouponCode code);

    /**
     * Finds a coupon by code string.
     */
    Optional<Coupon> findByCode(String code);

    /**
     * Finds all valid (active and not expired) coupons.
     */
    List<Coupon> findAllValid();

    /**
     * Finds all coupons.
     */
    List<Coupon> findAll();

    /**
     * Deletes a coupon.
     */
    void delete(UUID couponId);

    /**
     * Checks if a coupon code already exists.
     */
    boolean existsByCode(CouponCode code);
}
