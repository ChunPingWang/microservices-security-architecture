package com.ecommerce.product.application.dto;

import java.util.List;

/**
 * Response DTO for paginated product search results.
 */
public record ProductSearchResult(
        List<ProductResponse> products,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {
    public static ProductSearchResult of(
            List<ProductResponse> products,
            long totalElements,
            int currentPage,
            int pageSize
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new ProductSearchResult(products, totalElements, totalPages, currentPage, pageSize);
    }
}
