package com.ecommerce.admin.infrastructure.web.controllers;

import com.ecommerce.admin.application.dto.AdminAuthResponse;
import com.ecommerce.admin.application.dto.AdminLoginCommand;
import com.ecommerce.admin.application.usecases.AdminAuthUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin authentication.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthUseCase adminAuthUseCase;

    public AdminAuthController(AdminAuthUseCase adminAuthUseCase) {
        this.adminAuthUseCase = adminAuthUseCase;
    }

    /**
     * Authenticates an admin.
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginCommand command) {
        AdminAuthResponse response = adminAuthUseCase.authenticate(command);
        return ResponseEntity.ok(response);
    }
}
