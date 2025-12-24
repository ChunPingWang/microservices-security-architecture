package com.ecommerce.sales.infrastructure.web;

import com.ecommerce.sales.application.dto.PromotionResponse;
import com.ecommerce.sales.application.usecases.GetPromotionsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for promotion operations.
 */
@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final GetPromotionsUseCase getPromotionsUseCase;

    public PromotionController(GetPromotionsUseCase getPromotionsUseCase) {
        this.getPromotionsUseCase = getPromotionsUseCase;
    }

    /**
     * Get all active promotions.
     * GET /api/v1/promotions
     */
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        List<PromotionResponse> promotions = getPromotionsUseCase.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    /**
     * Get promotion by ID.
     * GET /api/v1/promotions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable UUID id) {
        PromotionResponse promotion = getPromotionsUseCase.getById(id);
        return ResponseEntity.ok(promotion);
    }
}
