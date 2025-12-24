package com.ecommerce.admin.infrastructure.web.controllers;

import com.ecommerce.admin.application.dto.CreatePromotionCommand;
import com.ecommerce.admin.application.dto.PromotionSummary;
import com.ecommerce.admin.application.usecases.PromotionManagementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for promotion management.
 */
@RestController
@RequestMapping("/api/admin/promotions")
public class PromotionManagementController {

    private final PromotionManagementUseCase promotionManagementUseCase;

    public PromotionManagementController(PromotionManagementUseCase promotionManagementUseCase) {
        this.promotionManagementUseCase = promotionManagementUseCase;
    }

    /**
     * Lists all promotions.
     * GET /api/admin/promotions
     */
    @GetMapping
    public ResponseEntity<List<PromotionSummary>> listPromotions(
            @RequestHeader("X-Admin-Id") UUID adminId) {

        List<PromotionSummary> promotions = promotionManagementUseCase.listPromotions(adminId);
        return ResponseEntity.ok(promotions);
    }

    /**
     * Creates a new promotion.
     * POST /api/admin/promotions
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPromotion(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestBody CreatePromotionCommand command) {

        UUID promotionId = promotionManagementUseCase.createPromotion(adminId, command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("promotionId", promotionId.toString()));
    }

    /**
     * Toggles promotion status.
     * PATCH /api/admin/promotions/{promotionId}/status
     */
    @PatchMapping("/{promotionId}/status")
    public ResponseEntity<Map<String, Object>> togglePromotionStatus(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID promotionId,
            @RequestBody Map<String, Boolean> request) {

        Boolean active = request.get("active");
        boolean success = promotionManagementUseCase.togglePromotionStatus(adminId, promotionId, active);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Deletes a promotion.
     * DELETE /api/admin/promotions/{promotionId}
     */
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<Void> deletePromotion(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @PathVariable UUID promotionId) {

        boolean success = promotionManagementUseCase.deletePromotion(adminId, promotionId);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
