package com.ecommerce.customer.integration;

import com.ecommerce.customer.application.dto.AuthenticateCommand;
import com.ecommerce.customer.application.dto.AuthenticationResponse;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for login flow and account lockout.
 * Uses H2 in-memory database for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DisplayName("Login Flow Integration Tests")
class LoginFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_PASSWORD = "Password123";

    @Nested
    @DisplayName("Successful Login")
    class SuccessfulLogin {

        @Test
        @DisplayName("should login with valid credentials")
        void shouldLoginWithValidCredentials() throws Exception {
            // Register a unique user for this test
            String uniqueEmail = "login-valid-" + System.currentTimeMillis() + "@example.com";
            RegisterCustomerCommand registerCommand = new RegisterCustomerCommand(
                    uniqueEmail,
                    TEST_PASSWORD,
                    "Valid",
                    "User",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerCommand)));

            AuthenticateCommand loginCommand = new AuthenticateCommand(
                    uniqueEmail,
                    TEST_PASSWORD
            );

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.customer.email").value(uniqueEmail))
                    .andReturn();

            AuthenticationResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    AuthenticationResponse.class
            );

            assertThat(response.accessToken()).isNotBlank();
            assertThat(response.refreshToken()).isNotBlank();
            assertThat(response.customer()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Failed Login")
    class FailedLogin {

        @Test
        @DisplayName("should reject invalid password")
        void shouldRejectInvalidPassword() throws Exception {
            String uniqueEmail = "login-invalid-" + System.currentTimeMillis() + "@example.com";
            RegisterCustomerCommand registerCommand = new RegisterCustomerCommand(
                    uniqueEmail,
                    TEST_PASSWORD,
                    "Invalid",
                    "Password",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerCommand)));

            AuthenticateCommand loginCommand = new AuthenticateCommand(
                    uniqueEmail,
                    "WrongPassword123"
            );

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginCommand)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("should reject non-existent email")
        void shouldRejectNonExistentEmail() throws Exception {
            AuthenticateCommand loginCommand = new AuthenticateCommand(
                    "nonexistent@example.com",
                    "Password123"
            );

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginCommand)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        }
    }

    @Nested
    @DisplayName("Account Lockout")
    class AccountLockout {

        @Test
        @DisplayName("should lock account after 5 failed attempts")
        @org.junit.jupiter.api.Disabled("Requires PostgreSQL/Testcontainers - H2 has timestamp handling issues")
        void shouldLockAccountAfterFiveFailedAttempts() throws Exception {
            String uniqueEmail = "lockout@example.com";
            RegisterCustomerCommand registerCommand = new RegisterCustomerCommand(
                    uniqueEmail,
                    TEST_PASSWORD,
                    "Lockout",
                    "Test",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerCommand)))
                    .andExpect(status().isCreated());

            AuthenticateCommand wrongPassword = new AuthenticateCommand(
                    uniqueEmail,
                    "WrongPass1"
            );

            // Attempt 5 failed logins - each should return 401
            for (int i = 1; i <= 5; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrongPassword)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
            }

            // 6th attempt should show account locked (423)
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongPassword)))
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"));

            // Even with correct password, account should remain locked
            AuthenticateCommand correctPassword = new AuthenticateCommand(
                    uniqueEmail,
                    TEST_PASSWORD
            );

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(correctPassword)))
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"));
        }

        @Test
        @DisplayName("should reset failed attempts after successful login")
        void shouldResetFailedAttemptsAfterSuccess() throws Exception {
            String uniqueEmail = "reset-" + System.currentTimeMillis() + "@example.com";
            RegisterCustomerCommand registerCommand = new RegisterCustomerCommand(
                    uniqueEmail,
                    TEST_PASSWORD,
                    "Reset",
                    "Test",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerCommand)));

            // 3 failed attempts
            AuthenticateCommand wrongPassword = new AuthenticateCommand(
                    uniqueEmail,
                    "WrongPassword"
            );

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrongPassword)))
                        .andExpect(status().isUnauthorized());
            }

            // Successful login
            AuthenticateCommand correctPassword = new AuthenticateCommand(
                    uniqueEmail,
                    TEST_PASSWORD
            );

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(correctPassword)))
                    .andExpect(status().isOk());

            // Now 4 more failed attempts should NOT lock the account
            for (int i = 0; i < 4; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrongPassword)))
                        .andExpect(status().isUnauthorized());
            }

            // Still not locked (only 4 failed since last success)
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(correctPassword)))
                    .andExpect(status().isOk());
        }
    }
}
