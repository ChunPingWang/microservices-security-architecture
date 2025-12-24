package com.ecommerce.sales.application.usecases;

import com.ecommerce.sales.application.dto.ApplyCouponCommand;
import com.ecommerce.sales.application.dto.CouponValidationResponse;
import com.ecommerce.sales.application.exceptions.CouponNotFoundException;
import com.ecommerce.sales.application.exceptions.CouponNotValidException;
import com.ecommerce.sales.domain.ports.CouponRepository;
import com.ecommerce.sales.domain.entities.Coupon;
import com.ecommerce.shared.domain.value_objects.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case for applying and validating coupons.
 */
@Service
public class ApplyCouponUseCase {

    private final CouponRepository couponRepository;

    public ApplyCouponUseCase(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * Validate a coupon without marking it as used.
     */
    public CouponValidationResponse validate(ApplyCouponCommand command) {
        return validate(command, null);
    }

    /**
     * Validate a coupon for a specific customer without marking it as used.
     */
    public CouponValidationResponse validate(ApplyCouponCommand command, UUID customerId) {
        Coupon coupon = couponRepository.findByCode(command.couponCode())
                .orElse(null);

        if (coupon == null) {
            return CouponValidationResponse.invalid(command.couponCode(), "Coupon not found");
        }

        // Check if coupon is valid
        if (!coupon.isValid()) {
            return CouponValidationResponse.invalid(command.couponCode(), "Coupon is expired or inactive");
        }

        // Check if coupon has remaining uses
        if (coupon.isExhausted()) {
            return CouponValidationResponse.invalid(command.couponCode(), "Coupon usage limit reached");
        }

        // Check per-customer limit if applicable
        if (customerId != null && !coupon.canBeUsedBy(customerId)) {
            return CouponValidationResponse.invalid(command.couponCode(), "You have already used this coupon the maximum number of times");
        }

        // Check minimum order amount
        Money orderTotal = Money.of(command.orderTotal());
        if (!coupon.getDiscountRule().meetsMinimum(orderTotal)) {
            BigDecimal minAmount = coupon.getDiscountRule().minimumOrderAmount().getAmount();
            return CouponValidationResponse.invalid(command.couponCode(),
                    "Order total must be at least $" + minAmount + " to use this coupon");
        }

        // Calculate discount
        Money discount = coupon.getDiscountRule().calculateDiscount(orderTotal);

        return CouponValidationResponse.valid(
                command.couponCode(),
                coupon.getDescription(),
                discount.getAmount()
        );
    }

    /**
     * Apply a coupon and mark it as used.
     */
    public CouponValidationResponse apply(ApplyCouponCommand command, UUID customerId) {
        // First validate
        CouponValidationResponse validation = validate(command, customerId);
        if (!validation.valid()) {
            return validation;
        }

        // Find and use the coupon
        Coupon coupon = couponRepository.findByCode(command.couponCode())
                .orElseThrow(() -> new CouponNotFoundException(command.couponCode()));

        coupon.use(customerId);
        couponRepository.save(coupon);

        return validation;
    }

    /**
     * Get coupon by code.
     */
    public Coupon getByCode(String couponCode) {
        return couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new CouponNotFoundException(couponCode));
    }
}
