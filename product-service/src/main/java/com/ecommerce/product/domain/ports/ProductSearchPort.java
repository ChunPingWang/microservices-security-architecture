package com.ecommerce.product.domain.ports;

import com.ecommerce.product.domain.entities.Product;

import java.util.List;
import java.util.UUID;

/**
 * Port for product search operations (e.g., Elasticsearch).
 */
public interface ProductSearchPort {

    /**
     * Indexes a product for search.
     */
    void index(Product product);

    /**
     * Removes a product from search index.
     */
    void remove(UUID productId);

    /**
     * Searches products by keyword.
     */
    List<Product> search(String keyword, int page, int size);

    /**
     * Searches products by keyword within a category.
     */
    List<Product> searchInCategory(String keyword, UUID categoryId, int page, int size);

    /**
     * Suggests products based on partial input.
     */
    List<String> suggest(String prefix, int limit);
}
