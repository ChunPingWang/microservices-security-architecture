package com.ecommerce.customer.contract;

import com.ecommerce.customer.application.dto.AuthenticateCommand;
import com.ecommerce.customer.application.dto.AuthenticationResponse;
import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.exceptions.AuthenticationFailedException;
import com.ecommerce.customer.application.usecases.AuthenticateCustomerUseCase;
import com.ecommerce.customer.application.usecases.RegisterCustomerUseCase;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import com.ecommerce.customer.infrastructure.web.controllers.AuthController;
import com.ecommerce.customer.infrastructure.web.handlers.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for POST /api/v1/auth/login endpoint.
 * These tests verify the API contract without requiring a full application context.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {AuthController.class, GlobalExceptionHandler.class})
@DisplayName("Authentication Contract Tests")
class AuthenticationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterCustomerUseCase registerCustomerUseCase;

    @MockBean
    private AuthenticateCustomerUseCase authenticateCustomerUseCase;

    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid login request")
        void shouldAcceptValidLoginRequest() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should accept any password format - validation is business logic")
        void shouldAcceptAnyPasswordFormat() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "anypassword"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Request Validation Contract")
    class RequestValidationContract {

        @Test
        @DisplayName("should reject request with missing email")
        void shouldRejectMissingEmail() throws Exception {
            String requestBody = """
                    {
                        "password": "Password123"
                    }
                    """;

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should reject request with empty email")
        void shouldRejectEmptyEmail() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "",
                    "Password123"
            );

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with invalid email format")
        void shouldRejectInvalidEmailFormat() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "invalid-email",
                    "Password123"
            );

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with missing password")
        void shouldRejectMissingPassword() throws Exception {
            String requestBody = """
                    {
                        "email": "user@example.com"
                    }
                    """;

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with empty password")
        void shouldRejectEmptyPassword() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    ""
            );

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject empty request body")
        void shouldRejectEmptyRequestBody() throws Exception {
            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("Response Contract - Success")
    class SuccessResponseContract {

        @Test
        @DisplayName("should return 200 OK on successful login")
        void shouldReturn200OnSuccess() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return authentication response with access token")
        void shouldReturnAccessToken() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.accessToken").isString());
        }

        @Test
        @DisplayName("should return authentication response with refresh token")
        void shouldReturnRefreshToken() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isString());
        }

        @Test
        @DisplayName("should return authentication response with customer details")
        void shouldReturnCustomerDetails() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customer").exists())
                    .andExpect(jsonPath("$.customer.id").exists())
                    .andExpect(jsonPath("$.customer.email").value("user@example.com"))
                    .andExpect(jsonPath("$.customer.firstName").value("John"))
                    .andExpect(jsonPath("$.customer.lastName").value("Doe"))
                    .andExpect(jsonPath("$.customer.memberLevel").exists());
        }

        @Test
        @DisplayName("should not expose password in response")
        void shouldNotExposePasswordInResponse() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenReturn(createMockAuthenticationResponse());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customer.password").doesNotExist())
                    .andExpect(jsonPath("$.customer.passwordHash").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Response Contract - Failure")
    class FailureResponseContract {

        @Test
        @DisplayName("should return 401 Unauthorized for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "WrongPassword"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenThrow(AuthenticationFailedException.invalidCredentials());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should return 401 Unauthorized for non-existent user")
        void shouldReturn401ForNonExistentUser() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "nonexistent@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenThrow(AuthenticationFailedException.invalidCredentials());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("should return 423 Locked for locked account")
        void shouldReturn423ForLockedAccount() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "locked@example.com",
                    "Password123"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenThrow(AuthenticationFailedException.accountLocked());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Error Response Contract")
    class ErrorResponseContract {

        @Test
        @DisplayName("should return error response with standard structure for validation errors")
        void shouldReturnStandardErrorStructureForValidation() throws Exception {
            String requestBody = """
                    {
                        "email": "invalid",
                        "password": ""
                    }
                    """;

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("should return error response with standard structure for auth errors")
        void shouldReturnStandardErrorStructureForAuthErrors() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "WrongPassword"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenThrow(AuthenticationFailedException.invalidCredentials());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("should not leak internal error details for security")
        void shouldNotLeakInternalErrorDetails() throws Exception {
            AuthenticateCommand command = new AuthenticateCommand(
                    "user@example.com",
                    "WrongPassword"
            );

            when(authenticateCustomerUseCase.execute(any(AuthenticateCommand.class)))
                    .thenThrow(AuthenticationFailedException.invalidCredentials());

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.cause").doesNotExist())
                    .andExpect(jsonPath("$.exception").doesNotExist());
        }
    }

    private AuthenticationResponse createMockAuthenticationResponse() {
        CustomerResponse customer = new CustomerResponse(
                UUID.randomUUID(),
                "user@example.com",
                "John",
                "Doe",
                "John Doe",
                "0912345678",
                MemberLevel.NORMAL,
                BigDecimal.ZERO,
                false,
                Instant.now()
        );

        return new AuthenticationResponse(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-access-token",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-refresh-token",
                customer
        );
    }
}
