package com.ecommerce.admin.contract;

import com.ecommerce.admin.application.dto.AdminAuthResponse;
import com.ecommerce.admin.application.dto.AdminLoginCommand;
import com.ecommerce.admin.application.exceptions.AdminAuthenticationException;
import com.ecommerce.admin.application.usecases.AdminAuthUseCase;
import com.ecommerce.admin.domain.value_objects.AdminRole;
import com.ecommerce.admin.infrastructure.web.controllers.AdminAuthController;
import com.ecommerce.admin.infrastructure.web.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Admin Authentication endpoints.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {AdminAuthController.class, GlobalExceptionHandler.class})
@DisplayName("Admin Auth Contract Tests")
class AdminAuthContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuthUseCase adminAuthUseCase;

    @Nested
    @DisplayName("Login Request Contract")
    class LoginRequestContract {

        @Test
        @DisplayName("should accept valid login request")
        void shouldAcceptValidLoginRequest() throws Exception {
            AdminAuthResponse response = new AdminAuthResponse(
                    UUID.randomUUID(),
                    "admin@ecommerce.com",
                    "系統管理員",
                    AdminRole.SUPER_ADMIN.name(),
                    List.of("MANAGE_PRODUCTS", "MANAGE_ORDERS", "MANAGE_ADMINS"),
                    "jwt-token-here"
            );
            when(adminAuthUseCase.authenticate(any(AdminLoginCommand.class))).thenReturn(response);

            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "email": "admin@ecommerce.com",
                                    "password": "SecurePass123"
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should require email")
        void shouldRequireEmail() throws Exception {
            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "password": "SecurePass123"
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should require password")
        void shouldRequirePassword() throws Exception {
            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "email": "admin@ecommerce.com"
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login Response Contract")
    class LoginResponseContract {

        @Test
        @DisplayName("should return admin details with token on success")
        void shouldReturnAdminDetailsWithTokenOnSuccess() throws Exception {
            UUID adminId = UUID.randomUUID();
            AdminAuthResponse response = new AdminAuthResponse(
                    adminId,
                    "admin@ecommerce.com",
                    "系統管理員",
                    AdminRole.SUPER_ADMIN.name(),
                    List.of("MANAGE_PRODUCTS", "MANAGE_ORDERS", "MANAGE_ADMINS"),
                    "jwt-token-here"
            );
            when(adminAuthUseCase.authenticate(any(AdminLoginCommand.class))).thenReturn(response);

            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "email": "admin@ecommerce.com",
                                    "password": "SecurePass123"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.adminId").value(adminId.toString()))
                    .andExpect(jsonPath("$.email").value("admin@ecommerce.com"))
                    .andExpect(jsonPath("$.name").value("系統管理員"))
                    .andExpect(jsonPath("$.role").value("SUPER_ADMIN"))
                    .andExpect(jsonPath("$.permissions").isArray())
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("should return 401 on invalid credentials")
        void shouldReturn401OnInvalidCredentials() throws Exception {
            when(adminAuthUseCase.authenticate(any(AdminLoginCommand.class)))
                    .thenThrow(new AdminAuthenticationException("Invalid credentials"));

            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "email": "admin@ecommerce.com",
                                    "password": "WrongPassword"
                                }
                                """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("AUTHENTICATION_FAILED"));
        }

        @Test
        @DisplayName("should return 401 when admin is deactivated")
        void shouldReturn401WhenAdminIsDeactivated() throws Exception {
            when(adminAuthUseCase.authenticate(any(AdminLoginCommand.class)))
                    .thenThrow(new AdminAuthenticationException("Account is deactivated"));

            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "email": "admin@ecommerce.com",
                                    "password": "SecurePass123"
                                }
                                """))
                    .andExpect(status().isUnauthorized());
        }
    }
}
