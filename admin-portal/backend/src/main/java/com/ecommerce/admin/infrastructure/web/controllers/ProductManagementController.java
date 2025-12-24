package com.ecommerce.admin.infrastructure.web.controllers;

import com.ecommerce.admin.application.dto.ProductSummary;
import com.ecommerce.admin.application.usecases.ProductManagementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for product management.
 */
@RestController
@RequestMapping("/api/admin/products")
public class ProductManagementController {

    private final ProductManagementUseCase productManagementUseCase;

    public ProductManagementController(ProductManagementUseCase productManagementUseCase) {
        this.productManagementUseCase = productManagementUseCase;
    }

    /**
     * Lists all products.
     * GET /api/admin/products
     */
    @GetMapping
    public ResponseEntity<List<ProductSummary>> listProducts(
            @RequestHeader("X-Admin-Id") UUID adminId) {
        List<ProductSummary> products = productManagementUseCase.listProducts(adminId);
        return ResponseEntity.ok(products);
    }

    /**
     * Updates product stock.
     * PATCH /api/admin/products/{productId}/stock
     */
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID productId,
            @RequestBody Map<String, Integer> request) {

        Integer quantity = request.get("quantity");
        boolean success = productManagementUseCase.updateStock(adminId, productId, quantity);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Toggles product status.
     * PATCH /api/admin/products/{productId}/status
     */
    @PatchMapping("/{productId}/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID productId,
            @RequestBody Map<String, Boolean> request) {

        Boolean active = request.get("active");
        boolean success = productManagementUseCase.toggleProductStatus(adminId, productId, active);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
