package com.ecommerce.sales.infrastructure.web;

import com.ecommerce.sales.application.exceptions.CouponNotFoundException;
import com.ecommerce.sales.application.exceptions.CouponNotValidException;
import com.ecommerce.sales.application.exceptions.PromotionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the sales service.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PromotionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePromotionNotFound(PromotionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Promotion not found",
                        "promotionId", ex.getPromotionId().toString(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCouponNotFound(CouponNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "COUPON_NOT_FOUND",
                        "couponCode", ex.getCouponCode(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(CouponNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleCouponNotValid(CouponNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "COUPON_NOT_VALID",
                        "couponCode", ex.getCouponCode(),
                        "message", ex.getReason(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Validation failed",
                        "details", errors,
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal server error",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }
}
