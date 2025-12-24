package com.ecommerce.admin.application.usecases;

import com.ecommerce.admin.application.dto.CreatePromotionCommand;
import com.ecommerce.admin.application.dto.PromotionSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use case for promotion management operations.
 * Note: In a real implementation, this would call the sales-service via Feign client.
 */
@Service
public class PromotionManagementUseCase {

    // Mock data store for demo purposes
    private final Map<UUID, PromotionSummary> promotions = new ConcurrentHashMap<>();

    public PromotionManagementUseCase() {
        // Initialize with some mock data
        UUID promo1 = UUID.randomUUID();
        UUID promo2 = UUID.randomUUID();

        promotions.put(promo1, new PromotionSummary(promo1, "雙11特賣", "PERCENTAGE",
                new BigDecimal("20"), true, LocalDateTime.now(), LocalDateTime.now().plusDays(7)));
        promotions.put(promo2, new PromotionSummary(promo2, "新會員優惠", "FIXED_AMOUNT",
                new BigDecimal("100"), true, LocalDateTime.now(), LocalDateTime.now().plusDays(30)));
    }

    /**
     * Lists all promotions.
     */
    public List<PromotionSummary> listPromotions(UUID adminId) {
        return new ArrayList<>(promotions.values());
    }

    /**
     * Creates a new promotion.
     */
    public UUID createPromotion(UUID adminId, CreatePromotionCommand command) {
        UUID promotionId = UUID.randomUUID();
        PromotionSummary promotion = new PromotionSummary(
                promotionId,
                command.name(),
                command.discountType(),
                command.discountValue(),
                true,
                command.startDate(),
                command.endDate()
        );
        promotions.put(promotionId, promotion);
        return promotionId;
    }

    /**
     * Toggles promotion status.
     */
    public boolean togglePromotionStatus(UUID adminId, UUID promotionId, Boolean active) {
        PromotionSummary existing = promotions.get(promotionId);
        if (existing == null) {
            return false;
        }

        PromotionSummary updated = new PromotionSummary(
                existing.promotionId(),
                existing.name(),
                existing.discountType(),
                existing.discountValue(),
                active,
                existing.startDate(),
                existing.endDate()
        );
        promotions.put(promotionId, updated);
        return true;
    }

    /**
     * Deletes a promotion.
     */
    public boolean deletePromotion(UUID adminId, UUID promotionId) {
        return promotions.remove(promotionId) != null;
    }
}
