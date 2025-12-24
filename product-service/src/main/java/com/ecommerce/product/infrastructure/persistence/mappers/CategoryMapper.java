package com.ecommerce.product.infrastructure.persistence.mappers;

import com.ecommerce.product.domain.entities.Category;
import com.ecommerce.product.infrastructure.persistence.entities.CategoryJpaEntity;

/**
 * Mapper between Category domain entity and JPA entity.
 */
public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toDomain(CategoryJpaEntity jpa) {
        return Category.reconstitute(
                jpa.getId(),
                jpa.getName(),
                jpa.getDescription(),
                jpa.getParentId(),
                jpa.getDisplayOrder(),
                jpa.isActive(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }

    public static CategoryJpaEntity toJpa(Category domain) {
        CategoryJpaEntity jpa = new CategoryJpaEntity();
        jpa.setId(domain.getId());
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setParentId(domain.getParentId());
        jpa.setDisplayOrder(domain.getDisplayOrder());
        jpa.setActive(domain.isActive());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        return jpa;
    }
}
