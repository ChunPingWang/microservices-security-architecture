package com.ecommerce.product.application.usecases;

import com.ecommerce.product.application.dto.CategoryResponse;
import com.ecommerce.product.application.exceptions.CategoryNotFoundException;
import com.ecommerce.product.domain.entities.Category;
import com.ecommerce.product.domain.ports.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for getting categories.
 */
@Service
public class GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Gets all root categories with their children.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllRoots() {
        List<Category> roots = categoryRepository.findAllRoots();

        return roots.stream()
                .map(this::buildCategoryTree)
                .toList();
    }

    /**
     * Gets all active categories (flat list).
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActive() {
        return categoryRepository.findAllActive().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    /**
     * Gets a category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryNotFoundException.byId(categoryId.toString()));

        return buildCategoryTree(category);
    }

    private CategoryResponse buildCategoryTree(Category category) {
        List<Category> children = categoryRepository.findByParentId(category.getId());

        if (children.isEmpty()) {
            return CategoryResponse.from(category);
        }

        List<CategoryResponse> childResponses = children.stream()
                .map(this::buildCategoryTree)
                .toList();

        return CategoryResponse.from(category, childResponses);
    }
}
