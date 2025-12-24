package com.ecommerce.product.infrastructure.persistence.adapters;

import com.ecommerce.product.domain.entities.Category;
import com.ecommerce.product.domain.ports.CategoryRepository;
import com.ecommerce.product.infrastructure.persistence.mappers.CategoryMapper;
import com.ecommerce.product.infrastructure.persistence.repositories.CategoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing CategoryRepository port using JPA.
 */
@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        var jpaEntity = CategoryMapper.toJpa(category);
        var saved = jpaRepository.save(jpaEntity);
        return CategoryMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(CategoryMapper::toDomain);
    }

    @Override
    public List<Category> findAllRoots() {
        return jpaRepository.findByParentIdIsNullOrderByDisplayOrder().stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return jpaRepository.findByParentIdOrderByDisplayOrder(parentId).stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllActive() {
        return jpaRepository.findByActiveTrueOrderByDisplayOrder().stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
