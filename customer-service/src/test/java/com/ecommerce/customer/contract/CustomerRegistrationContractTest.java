package com.ecommerce.customer.contract;

import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.ecommerce.customer.application.exceptions.EmailAlreadyExistsException;
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
 * Contract tests for POST /api/v1/auth/register endpoint.
 * These tests verify the API contract without requiring a full application context.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {AuthController.class, GlobalExceptionHandler.class})
@DisplayName("Customer Registration Contract Tests")
class CustomerRegistrationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterCustomerUseCase registerCustomerUseCase;

    @MockBean
    private AuthenticateCustomerUseCase authenticateCustomerUseCase;

    private static final String REGISTER_ENDPOINT = "/api/v1/auth/register";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid registration request with all fields")
        void shouldAcceptValidRequestWithAllFields() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    "0912345678"
            );

            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenReturn(createMockCustomerResponse());

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should accept valid registration request without optional phone")
        void shouldAcceptValidRequestWithoutOptionalPhone() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenReturn(createMockCustomerResponse());

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated());
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
                        "password": "Password123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """;

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should reject request with invalid email format")
        void shouldRejectInvalidEmailFormat() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "invalid-email",
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post(REGISTER_ENDPOINT)
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
                        "email": "user@example.com",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """;

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with weak password - too short")
        void shouldRejectWeakPasswordTooShort() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Pass1",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with weak password - no uppercase")
        void shouldRejectWeakPasswordNoUppercase() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "password123",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with weak password - no digit")
        void shouldRejectWeakPasswordNoDigit() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "PasswordABC",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with missing first name")
        void shouldRejectMissingFirstName() throws Exception {
            String requestBody = """
                    {
                        "email": "user@example.com",
                        "password": "Password123",
                        "lastName": "Doe"
                    }
                    """;

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with missing last name")
        void shouldRejectMissingLastName() throws Exception {
            String requestBody = """
                    {
                        "email": "user@example.com",
                        "password": "Password123",
                        "firstName": "John"
                    }
                    """;

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject request with invalid phone format")
        void shouldRejectInvalidPhoneFormat() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    "123456789"
            );

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("Response Contract")
    class ResponseContract {

        @Test
        @DisplayName("should return 201 Created on successful registration")
        void shouldReturn201OnSuccess() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    "0912345678"
            );

            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenReturn(createMockCustomerResponse());

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should return customer response with required fields")
        void shouldReturnCustomerResponseWithRequiredFields() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    "0912345678"
            );

            CustomerResponse mockResponse = createMockCustomerResponse();
            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.fullName").value("John Doe"))
                    .andExpect(jsonPath("$.memberLevel").value("NORMAL"))
                    .andExpect(jsonPath("$.totalSpending").value(0))
                    .andExpect(jsonPath("$.emailVerified").value(false))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("should not expose password in response")
        void shouldNotExposePasswordInResponse() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "user@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenReturn(createMockCustomerResponse());

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        @DisplayName("should return 409 Conflict for duplicate email")
        void shouldReturn409ForDuplicateEmail() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "existing@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenThrow(new EmailAlreadyExistsException("existing@example.com"));

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"))
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
                        "password": "weak",
                        "firstName": "",
                        "lastName": ""
                    }
                    """;

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("should return error response with standard structure for business errors")
        void shouldReturnStandardErrorStructureForBusinessErrors() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "existing@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            when(registerCustomerUseCase.execute(any(RegisterCustomerCommand.class)))
                    .thenThrow(new EmailAlreadyExistsException("existing@example.com"));

            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    private CustomerResponse createMockCustomerResponse() {
        return new CustomerResponse(
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
    }
}
