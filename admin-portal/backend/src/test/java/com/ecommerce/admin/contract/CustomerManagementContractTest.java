package com.ecommerce.admin.contract;

import com.ecommerce.admin.application.dto.CustomerSummary;
import com.ecommerce.admin.application.usecases.CustomerManagementUseCase;
import com.ecommerce.admin.infrastructure.web.controllers.CustomerManagementController;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for Customer Management endpoints.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CustomerManagementController.class, GlobalExceptionHandler.class})
@DisplayName("Customer Management Contract Tests")
class CustomerManagementContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerManagementUseCase customerManagementUseCase;

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("List Customers")
    class ListCustomers {

        @Test
        @DisplayName("should return list of customers")
        void shouldReturnListOfCustomers() throws Exception {
            List<CustomerSummary> customers = List.of(
                    new CustomerSummary(UUID.randomUUID(), "user1@example.com", "王小明",
                            "GOLD", new BigDecimal("50000"), true, LocalDateTime.now()),
                    new CustomerSummary(UUID.randomUUID(), "user2@example.com", "李小華",
                            "SILVER", new BigDecimal("15000"), true, LocalDateTime.now())
            );
            when(customerManagementUseCase.listCustomers(any())).thenReturn(customers);

            mockMvc.perform(get("/api/admin/customers")
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].customerId").exists())
                    .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                    .andExpect(jsonPath("$[0].name").value("王小明"))
                    .andExpect(jsonPath("$[0].memberLevel").value("GOLD"))
                    .andExpect(jsonPath("$[0].totalSpending").value(50000))
                    .andExpect(jsonPath("$[0].active").value(true));
        }

        @Test
        @DisplayName("should filter customers by member level")
        void shouldFilterCustomersByMemberLevel() throws Exception {
            List<CustomerSummary> customers = List.of(
                    new CustomerSummary(UUID.randomUUID(), "vip@example.com", "張大戶",
                            "PLATINUM", new BigDecimal("200000"), true, LocalDateTime.now())
            );
            when(customerManagementUseCase.listCustomersByMemberLevel(any(), eq("PLATINUM"))).thenReturn(customers);

            mockMvc.perform(get("/api/admin/customers")
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .param("memberLevel", "PLATINUM"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].memberLevel").value("PLATINUM"));
        }
    }

    @Nested
    @DisplayName("Get Customer Detail")
    class GetCustomerDetail {

        @Test
        @DisplayName("should return customer details")
        void shouldReturnCustomerDetails() throws Exception {
            UUID customerId = UUID.randomUUID();
            CustomerSummary customer = new CustomerSummary(customerId, "user@example.com", "王小明",
                    "GOLD", new BigDecimal("50000"), true, LocalDateTime.now());
            when(customerManagementUseCase.getCustomer(any(), eq(customerId))).thenReturn(customer);

            mockMvc.perform(get("/api/admin/customers/{customerId}", customerId)
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("王小明"));
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(customerManagementUseCase.getCustomer(any(), eq(customerId))).thenReturn(null);

            mockMvc.perform(get("/api/admin/customers/{customerId}", customerId)
                            .header("X-Admin-Id", ADMIN_ID.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Toggle Customer Status")
    class ToggleCustomerStatus {

        @Test
        @DisplayName("should deactivate customer account")
        void shouldDeactivateCustomerAccount() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(customerManagementUseCase.toggleCustomerStatus(any(), eq(customerId), eq(false))).thenReturn(true);

            mockMvc.perform(patch("/api/admin/customers/{customerId}/status", customerId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": false
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should activate customer account")
        void shouldActivateCustomerAccount() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(customerManagementUseCase.toggleCustomerStatus(any(), eq(customerId), eq(true))).thenReturn(true);

            mockMvc.perform(patch("/api/admin/customers/{customerId}/status", customerId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": true
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(customerManagementUseCase.toggleCustomerStatus(any(), eq(customerId), any())).thenReturn(false);

            mockMvc.perform(patch("/api/admin/customers/{customerId}/status", customerId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "active": false
                                }
                                """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update Member Level")
    class UpdateMemberLevel {

        @Test
        @DisplayName("should update member level")
        void shouldUpdateMemberLevel() throws Exception {
            UUID customerId = UUID.randomUUID();
            when(customerManagementUseCase.updateMemberLevel(any(), eq(customerId), eq("PLATINUM"))).thenReturn(true);

            mockMvc.perform(patch("/api/admin/customers/{customerId}/member-level", customerId)
                            .header("X-Admin-Id", ADMIN_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "memberLevel": "PLATINUM"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
