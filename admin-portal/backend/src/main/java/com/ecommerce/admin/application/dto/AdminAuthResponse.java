package com.ecommerce.admin.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for admin authentication.
 */
public record AdminAuthResponse(
        UUID adminId,
        String email,
        String name,
        String role,
        List<String> permissions,
        String token
) {}
