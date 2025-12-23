package com.ecommerce.product.infrastructure.web.controllers;

import com.ecommerce.product.application.dto.CategoryResponse;
import com.ecommerce.product.application.usecases.GetCategoriesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for category operations.
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final GetCategoriesUseCase getCategoriesUseCase;

    public CategoryController(GetCategoriesUseCase getCategoriesUseCase) {
        this.getCategoriesUseCase = getCategoriesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> categories = getCategoriesUseCase.getAllRoots();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        List<CategoryResponse> categories = getCategoriesUseCase.getAllActive();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID categoryId) {
        CategoryResponse category = getCategoriesUseCase.getById(categoryId);
        return ResponseEntity.ok(category);
    }
}
