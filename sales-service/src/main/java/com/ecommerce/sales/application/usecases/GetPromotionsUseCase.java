package com.ecommerce.sales.application.usecases;

import com.ecommerce.sales.application.dto.PromotionResponse;
import com.ecommerce.sales.domain.ports.PromotionRepository;
import com.ecommerce.sales.domain.aggregates.Promotion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving promotions.
 */
@Service
public class GetPromotionsUseCase {

    private final PromotionRepository promotionRepository;

    public GetPromotionsUseCase(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    /**
     * Get all active promotions.
     */
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findAllActive().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get promotion by ID.
     */
    public PromotionResponse getById(UUID promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new com.ecommerce.sales.application.exceptions.PromotionNotFoundException(promotionId));
        return toResponse(promotion);
    }

    private PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getDiscountRule().type().name(),
                promotion.getDiscountRule().value(),
                promotion.getDiscountRule().minimumOrderAmount() != null
                        ? promotion.getDiscountRule().minimumOrderAmount().getAmount()
                        : null,
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive()
        );
    }
}
