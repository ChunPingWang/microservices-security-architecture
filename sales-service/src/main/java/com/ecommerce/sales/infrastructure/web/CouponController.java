package com.ecommerce.sales.infrastructure.web;

import com.ecommerce.sales.application.dto.ApplyCouponCommand;
import com.ecommerce.sales.application.dto.CouponValidationResponse;
import com.ecommerce.sales.application.usecases.ApplyCouponUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for coupon operations.
 */
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final ApplyCouponUseCase applyCouponUseCase;

    public CouponController(ApplyCouponUseCase applyCouponUseCase) {
        this.applyCouponUseCase = applyCouponUseCase;
    }

    /**
     * Validate a coupon.
     * POST /api/v1/coupons/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody ApplyCouponCommand command,
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId) {
        CouponValidationResponse response = applyCouponUseCase.validate(command, customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply a coupon (validate and mark as used).
     * POST /api/v1/coupons/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<CouponValidationResponse> applyCoupon(
            @Valid @RequestBody ApplyCouponCommand command,
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId) {
        CouponValidationResponse response = applyCouponUseCase.apply(command, customerId);
        if (!response.valid()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
