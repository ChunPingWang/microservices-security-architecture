package com.ecommerce.product.infrastructure.search;

import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.ProductRepository;
import com.ecommerce.product.domain.ports.ProductSearchPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ProductSearchPort.
 * For production, replace with Elasticsearch adapter.
 */
@Component
public class InMemoryProductSearchAdapter implements ProductSearchPort {

    private final ConcurrentHashMap<UUID, Product> index = new ConcurrentHashMap<>();
    private final ProductRepository productRepository;

    public InMemoryProductSearchAdapter(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void index(Product product) {
        index.put(product.getId(), product);
    }

    @Override
    public void remove(UUID productId) {
        index.remove(productId);
    }

    @Override
    public List<Product> search(String keyword, int page, int size) {
        String lowerKeyword = keyword.toLowerCase();

        return index.values().stream()
                .filter(Product::isActive)
                .filter(p -> matchesKeyword(p, lowerKeyword))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public List<Product> searchInCategory(String keyword, UUID categoryId, int page, int size) {
        String lowerKeyword = keyword.toLowerCase();

        return index.values().stream()
                .filter(Product::isActive)
                .filter(p -> p.getCategoryId().equals(categoryId))
                .filter(p -> matchesKeyword(p, lowerKeyword))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public List<String> suggest(String prefix, int limit) {
        String lowerPrefix = prefix.toLowerCase();

        return index.values().stream()
                .filter(Product::isActive)
                .filter(p -> p.getName().toLowerCase().startsWith(lowerPrefix))
                .map(Product::getName)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        return index.values().stream()
                .filter(Product::isActive)
                .filter(p -> matchesKeyword(p, lowerKeyword))
                .count();
    }

    @Override
    public long countInCategory(String keyword, UUID categoryId) {
        String lowerKeyword = keyword.toLowerCase();

        return index.values().stream()
                .filter(Product::isActive)
                .filter(p -> p.getCategoryId().equals(categoryId))
                .filter(p -> matchesKeyword(p, lowerKeyword))
                .count();
    }

    private boolean matchesKeyword(Product product, String keyword) {
        return product.getName().toLowerCase().contains(keyword)
                || (product.getDescription() != null
                && product.getDescription().toLowerCase().contains(keyword))
                || product.getSku().getValue().toLowerCase().contains(keyword);
    }
}
