package com.ecommerce.product.infrastructure.web.controllers;

import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.ProductSearchResult;
import com.ecommerce.product.application.usecases.BrowseProductsUseCase;
import com.ecommerce.product.application.usecases.GetProductDetailUseCase;
import com.ecommerce.product.application.usecases.SearchProductsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for product operations.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final BrowseProductsUseCase browseProductsUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;

    public ProductController(
            BrowseProductsUseCase browseProductsUseCase,
            SearchProductsUseCase searchProductsUseCase,
            GetProductDetailUseCase getProductDetailUseCase) {
        this.browseProductsUseCase = browseProductsUseCase;
        this.searchProductsUseCase = searchProductsUseCase;
        this.getProductDetailUseCase = getProductDetailUseCase;
    }

    @GetMapping
    public ResponseEntity<?> browseProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (categoryId != null) {
            List<ProductResponse> products = browseProductsUseCase.executeByCategory(categoryId);
            return ResponseEntity.ok(products);
        } else {
            ProductSearchResult result = browseProductsUseCase.execute(page, size);
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResult> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductSearchResult result;
        if (categoryId != null) {
            result = searchProductsUseCase.executeInCategory(keyword, categoryId, page, size);
        } else {
            result = searchProductsUseCase.execute(keyword, page, size);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") int limit) {
        List<String> suggestions = searchProductsUseCase.getSuggestions(prefix, limit);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable UUID productId) {
        ProductResponse response = getProductDetailUseCase.execute(productId);
        return ResponseEntity.ok(response);
    }
}
