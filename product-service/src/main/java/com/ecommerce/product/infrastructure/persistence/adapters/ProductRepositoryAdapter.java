package com.ecommerce.product.infrastructure.persistence.adapters;

import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.ProductRepository;
import com.ecommerce.product.domain.value_objects.SKU;
import com.ecommerce.product.infrastructure.persistence.mappers.ProductMapper;
import com.ecommerce.product.infrastructure.persistence.repositories.ProductJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ProductRepository port using JPA.
 */
@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        var jpaEntity = ProductMapper.toJpa(product);
        var saved = jpaRepository.save(jpaEntity);
        return ProductMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(ProductMapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(SKU sku) {
        return jpaRepository.findBySku(sku.getValue())
                .map(ProductMapper::toDomain);
    }

    @Override
    public List<Product> findByCategory(UUID categoryId) {
        return jpaRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(ProductMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAllActive(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jpaRepository.findByActiveTrue(pageable).stream()
                .map(ProductMapper::toDomain)
                .toList();
    }

    @Override
    public long countActive() {
        return jpaRepository.countByActiveTrue();
    }

    @Override
    public boolean existsBySku(SKU sku) {
        return jpaRepository.existsBySku(sku.getValue());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
