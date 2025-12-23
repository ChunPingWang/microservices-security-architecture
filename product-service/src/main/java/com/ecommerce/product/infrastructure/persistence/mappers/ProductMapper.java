package com.ecommerce.product.infrastructure.persistence.mappers;

import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.value_objects.SKU;
import com.ecommerce.product.infrastructure.persistence.entities.ProductJpaEntity;
import com.ecommerce.shared.domain.value_objects.Money;

import java.util.ArrayList;
import java.util.Currency;

/**
 * Mapper between Product domain entity and JPA entity.
 */
public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toDomain(ProductJpaEntity jpa) {
        return Product.reconstitute(
                jpa.getId(),
                SKU.of(jpa.getSku()),
                jpa.getName(),
                jpa.getDescription(),
                Money.of(jpa.getPrice(), Currency.getInstance(jpa.getCurrency())),
                jpa.getCategoryId(),
                jpa.isActive(),
                new ArrayList<>(jpa.getImageUrls()),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt(),
                jpa.getVersion()
        );
    }

    public static ProductJpaEntity toJpa(Product domain) {
        ProductJpaEntity jpa = new ProductJpaEntity();
        jpa.setId(domain.getId());
        jpa.setSku(domain.getSku().getValue());
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        jpa.setPrice(domain.getPrice().getAmount());
        jpa.setCurrency(domain.getPrice().getCurrency().getCurrencyCode());
        jpa.setCategoryId(domain.getCategoryId());
        jpa.setActive(domain.isActive());
        jpa.setImageUrls(new ArrayList<>(domain.getImageUrls()));
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        return jpa;
    }
}
