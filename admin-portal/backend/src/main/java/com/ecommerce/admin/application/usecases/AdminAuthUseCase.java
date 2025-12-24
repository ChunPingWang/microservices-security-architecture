package com.ecommerce.admin.application.usecases;

import com.ecommerce.admin.application.dto.AdminAuthResponse;
import com.ecommerce.admin.application.dto.AdminLoginCommand;
import com.ecommerce.admin.application.exceptions.AdminAuthenticationException;
import com.ecommerce.admin.domain.entities.Admin;
import com.ecommerce.admin.domain.ports.AdminRepository;
import com.ecommerce.security.provider.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Use case for admin authentication.
 */
@Service
public class AdminAuthUseCase {

    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminAuthUseCase(AdminRepository adminRepository, JwtTokenProvider jwtTokenProvider) {
        this.adminRepository = adminRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticates an admin and returns a JWT token.
     */
    public AdminAuthResponse authenticate(AdminLoginCommand command) {
        Admin admin = adminRepository.findByEmail(command.email())
                .orElseThrow(() -> new AdminAuthenticationException("Invalid credentials"));

        if (!admin.authenticate(command.password())) {
            throw new AdminAuthenticationException("Invalid credentials");
        }

        if (!admin.isActive()) {
            throw new AdminAuthenticationException("Account is deactivated");
        }

        admin.recordLogin();
        adminRepository.save(admin);

        String token = jwtTokenProvider.generateAccessToken(
                admin.getId().toString(),
                admin.getEmail(),
                admin.getRole().name()
        );

        return new AdminAuthResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getName(),
                admin.getRole().name(),
                new ArrayList<>(admin.getRole().getPermissions()),
                token
        );
    }
}
