package com.ecommerce.admin.domain.ports;

import com.ecommerce.admin.domain.entities.Admin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Admin entities.
 */
public interface AdminRepository {

    Admin save(Admin admin);

    Optional<Admin> findById(UUID id);

    Optional<Admin> findByEmail(String email);

    List<Admin> findAll();

    boolean existsByEmail(String email);

    void delete(UUID id);
}
