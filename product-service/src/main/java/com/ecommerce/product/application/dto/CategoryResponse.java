package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.entities.Category;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for category data.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String description,
        UUID parentId,
        int displayOrder,
        boolean active,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                category.getDisplayOrder(),
                category.isActive(),
                List.of()
        );
    }

    public static CategoryResponse from(Category category, List<CategoryResponse> children) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                category.getDisplayOrder(),
                category.isActive(),
                children
        );
    }
}
