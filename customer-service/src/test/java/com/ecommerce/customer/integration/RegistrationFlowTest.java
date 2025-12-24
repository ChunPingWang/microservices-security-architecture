package com.ecommerce.customer.integration;

import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for customer registration flow.
 * Uses H2 in-memory database for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Registration Flow Integration Tests")
class RegistrationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Successful Registration")
    class SuccessfulRegistration {

        @Test
        @DisplayName("should register new customer with valid data")
        void shouldRegisterNewCustomer() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "newuser@example.com",
                    "Password123",
                    "John",
                    "Doe",
                    "0912345678"
            );

            MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.memberLevel").value("NORMAL"))
                    .andReturn();

            CustomerResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    CustomerResponse.class
            );

            assertThat(response.id()).isNotNull();
            assertThat(response.fullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("should initialize customer with zero spending")
        void shouldInitializeWithZeroSpending() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "zerospend@example.com",
                    "Password123",
                    "Jane",
                    "Smith",
                    "0987654321"
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalSpending").value(0));
        }
    }

    @Nested
    @DisplayName("Duplicate Email Prevention")
    class DuplicateEmailPrevention {

        @Test
        @DisplayName("should reject registration with existing email")
        void shouldRejectDuplicateEmail() throws Exception {
            String email = "duplicate@example.com";
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    email,
                    "Password123",
                    "First",
                    "User",
                    null
            );

            // First registration should succeed
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isCreated());

            // Second registration with same email should fail
            RegisterCustomerCommand duplicateCommand = new RegisterCustomerCommand(
                    email,
                    "Password456",
                    "Second",
                    "User",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateCommand)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("should reject invalid email format")
        void shouldRejectInvalidEmail() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "invalid-email",
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should reject weak password")
        void shouldRejectWeakPassword() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    "weakpass@example.com",
                    "weak",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should reject missing required fields")
        void shouldRejectMissingFields() throws Exception {
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    null,
                    "Password123",
                    "John",
                    "Doe",
                    null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }
}
