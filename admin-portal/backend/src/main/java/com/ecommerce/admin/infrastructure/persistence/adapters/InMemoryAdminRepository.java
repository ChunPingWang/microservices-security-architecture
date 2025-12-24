package com.ecommerce.admin.infrastructure.persistence.adapters;

import com.ecommerce.admin.domain.entities.Admin;
import com.ecommerce.admin.domain.ports.AdminRepository;
import com.ecommerce.admin.domain.value_objects.AdminRole;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of AdminRepository for development and testing.
 */
@Repository
public class InMemoryAdminRepository implements AdminRepository {

    private final Map<UUID, Admin> admins = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Create default super admin
        Admin superAdmin = Admin.create(
                "admin@ecommerce.com",
                "Admin123!",
                "系統管理員",
                AdminRole.SUPER_ADMIN
        );
        admins.put(superAdmin.getId(), superAdmin);
    }

    @Override
    public Admin save(Admin admin) {
        admins.put(admin.getId(), admin);
        return admin;
    }

    @Override
    public Optional<Admin> findById(UUID id) {
        return Optional.ofNullable(admins.get(id));
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return admins.values().stream()
                .filter(admin -> admin.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<Admin> findAll() {
        return new ArrayList<>(admins.values());
    }

    @Override
    public boolean existsByEmail(String email) {
        return admins.values().stream()
                .anyMatch(admin -> admin.getEmail().equals(email));
    }

    @Override
    public void delete(UUID id) {
        admins.remove(id);
    }

    /**
     * Clears all admins (for testing).
     */
    public void clear() {
        admins.clear();
    }
}
