package com.ecommerce.customer.infrastructure.persistence.mappers;

import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import com.ecommerce.customer.domain.value_objects.Password;
import com.ecommerce.customer.infrastructure.persistence.entities.CustomerJpaEntity;

/**
 * Mapper between Customer domain entity and JPA entity.
 */
public final class CustomerMapper {

    private CustomerMapper() {
    }

    /**
     * Converts a JPA entity to a domain entity.
     */
    public static Customer toDomain(CustomerJpaEntity jpa) {
        return Customer.reconstitute(
                jpa.getId(),
                Email.of(jpa.getEmail()),
                Password.fromHash(jpa.getPasswordHash()),
                jpa.getFirstName(),
                jpa.getLastName(),
                jpa.getPhoneNumber(),
                toMemberLevel(jpa.getMemberLevel()),
                jpa.getTotalSpending(),
                jpa.getFailedLoginAttempts(),
                jpa.getLockedUntil(),
                jpa.isEmailVerified(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt(),
                jpa.getVersion()
        );
    }

    /**
     * Converts a domain entity to a JPA entity.
     */
    public static CustomerJpaEntity toJpa(Customer domain) {
        CustomerJpaEntity jpa = new CustomerJpaEntity();
        jpa.setId(domain.getId());
        jpa.setEmail(domain.getEmail().getValue());
        jpa.setPasswordHash(domain.getPassword().getHash());
        jpa.setFirstName(domain.getFirstName());
        jpa.setLastName(domain.getLastName());
        jpa.setPhoneNumber(domain.getPhoneNumber());
        jpa.setMemberLevel(toMemberLevelJpa(domain.getMemberLevel()));
        jpa.setTotalSpending(domain.getTotalSpending());
        jpa.setFailedLoginAttempts(domain.getFailedLoginAttempts());
        jpa.setLockedUntil(domain.getLockedUntil());
        jpa.setEmailVerified(domain.isEmailVerified());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        return jpa;
    }

    private static MemberLevel toMemberLevel(CustomerJpaEntity.MemberLevelJpa jpa) {
        return switch (jpa) {
            case NORMAL -> MemberLevel.NORMAL;
            case SILVER -> MemberLevel.SILVER;
            case GOLD -> MemberLevel.GOLD;
            case PLATINUM -> MemberLevel.PLATINUM;
        };
    }

    private static CustomerJpaEntity.MemberLevelJpa toMemberLevelJpa(MemberLevel domain) {
        return switch (domain) {
            case NORMAL -> CustomerJpaEntity.MemberLevelJpa.NORMAL;
            case SILVER -> CustomerJpaEntity.MemberLevelJpa.SILVER;
            case GOLD -> CustomerJpaEntity.MemberLevelJpa.GOLD;
            case PLATINUM -> CustomerJpaEntity.MemberLevelJpa.PLATINUM;
        };
    }
}
