package com.ecommerce.product.infrastructure.persistence.mappers;

import com.ecommerce.product.domain.entities.Inventory;
import com.ecommerce.product.domain.value_objects.Stock;
import com.ecommerce.product.infrastructure.persistence.entities.InventoryJpaEntity;

/**
 * Mapper between Inventory domain entity and JPA entity.
 */
public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static Inventory toDomain(InventoryJpaEntity jpa) {
        return Inventory.reconstitute(
                jpa.getId(),
                jpa.getProductId(),
                Stock.of(jpa.getQuantity()),
                jpa.getLowStockThreshold(),
                jpa.getReservedQuantity(),
                jpa.getLastRestockedAt(),
                jpa.getUpdatedAt(),
                jpa.getVersion()
        );
    }

    public static InventoryJpaEntity toJpa(Inventory domain) {
        InventoryJpaEntity jpa = new InventoryJpaEntity();
        jpa.setId(domain.getId());
        jpa.setProductId(domain.getProductId());
        jpa.setQuantity(domain.getTotalQuantity());
        jpa.setLowStockThreshold(domain.getLowStockThreshold());
        jpa.setReservedQuantity(domain.getReservedQuantity());
        jpa.setLastRestockedAt(domain.getLastRestockedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        return jpa;
    }
}
