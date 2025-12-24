package com.ecommerce.customer.infrastructure.persistence.mappers;

import com.ecommerce.customer.domain.entities.Address;
import com.ecommerce.customer.infrastructure.persistence.entities.AddressJpaEntity;

/**
 * Mapper between Address domain entity and JPA entity.
 */
public final class AddressMapper {

    private AddressMapper() {
    }

    /**
     * Converts a JPA entity to a domain entity.
     */
    public static Address toDomain(AddressJpaEntity jpa) {
        return Address.reconstitute(
                jpa.getId(),
                jpa.getCustomerId(),
                jpa.getRecipientName(),
                jpa.getPhoneNumber(),
                jpa.getPostalCode(),
                jpa.getCity(),
                jpa.getDistrict(),
                jpa.getStreet(),
                jpa.getCountry(),
                jpa.isDefault(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }

    /**
     * Converts a domain entity to a JPA entity.
     */
    public static AddressJpaEntity toJpa(Address domain) {
        AddressJpaEntity jpa = new AddressJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCustomerId(domain.getCustomerId());
        jpa.setRecipientName(domain.getRecipientName());
        jpa.setPhoneNumber(domain.getPhoneNumber());
        jpa.setPostalCode(domain.getPostalCode());
        jpa.setCity(domain.getCity());
        jpa.setDistrict(domain.getDistrict());
        jpa.setStreet(domain.getStreet());
        jpa.setCountry(domain.getCountry());
        jpa.setDefault(domain.isDefault());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        return jpa;
    }
}
