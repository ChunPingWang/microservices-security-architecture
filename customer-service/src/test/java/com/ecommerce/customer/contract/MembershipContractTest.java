package com.ecommerce.customer.contract;

import com.ecommerce.customer.application.dto.MembershipResponse;
import com.ecommerce.customer.application.usecases.GetMembershipUseCase;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import com.ecommerce.customer.infrastructure.web.controllers.CustomerController;
import com.ecommerce.customer.infrastructure.web.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/customers/me/membership endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {CustomerController.class, GlobalExceptionHandler.class})
@DisplayName("Membership Contract Tests")
class MembershipContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetMembershipUseCase getMembershipUseCase;

    @MockBean
    private com.ecommerce.customer.application.usecases.RegisterCustomerUseCase registerCustomerUseCase;

    @MockBean
    private com.ecommerce.customer.application.usecases.GetCustomerProfileUseCase getCustomerProfileUseCase;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Response Contract")
    class ResponseContract {

        @Test
        @DisplayName("should return membership details for authenticated customer")
        void shouldReturnMembershipDetails() throws Exception {
            MembershipResponse response = new MembershipResponse(
                    CUSTOMER_ID,
                    MemberLevel.SILVER.name(),
                    new BigDecimal("15000.00"),
                    new BigDecimal("15000.00"),
                    3,
                    "銀卡會員專享折扣"
            );
            when(getMembershipUseCase.getMembership(CUSTOMER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/customers/me/membership")
                            .header("X-Customer-Id", CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.memberLevel").value("SILVER"))
                    .andExpect(jsonPath("$.totalSpending").value(15000.00))
                    .andExpect(jsonPath("$.spendingToNextLevel").value(15000.00))
                    .andExpect(jsonPath("$.discountPercentage").value(3))
                    .andExpect(jsonPath("$.benefitDescription").exists());
        }

        @Test
        @DisplayName("should return NORMAL level for new customers")
        void shouldReturnNormalLevelForNewCustomers() throws Exception {
            MembershipResponse response = new MembershipResponse(
                    CUSTOMER_ID,
                    MemberLevel.NORMAL.name(),
                    BigDecimal.ZERO,
                    new BigDecimal("10000.00"),
                    0,
                    "歡迎加入！消費滿 10,000 元即可升級為銀卡會員"
            );
            when(getMembershipUseCase.getMembership(CUSTOMER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/customers/me/membership")
                            .header("X-Customer-Id", CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberLevel").value("NORMAL"))
                    .andExpect(jsonPath("$.totalSpending").value(0))
                    .andExpect(jsonPath("$.discountPercentage").value(0));
        }

        @Test
        @DisplayName("should return PLATINUM level with no next level")
        void shouldReturnPlatinumLevelWithNoNextLevel() throws Exception {
            MembershipResponse response = new MembershipResponse(
                    CUSTOMER_ID,
                    MemberLevel.PLATINUM.name(),
                    new BigDecimal("150000.00"),
                    BigDecimal.ZERO,
                    10,
                    "尊榮白金會員專享10%折扣及優先服務"
            );
            when(getMembershipUseCase.getMembership(CUSTOMER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/customers/me/membership")
                            .header("X-Customer-Id", CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberLevel").value("PLATINUM"))
                    .andExpect(jsonPath("$.spendingToNextLevel").value(0))
                    .andExpect(jsonPath("$.discountPercentage").value(10));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should return 401 when no customer ID provided")
        void shouldReturn401WhenNoCustomerId() throws Exception {
            mockMvc.perform(get("/api/customers/me/membership"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            when(getMembershipUseCase.getMembership(CUSTOMER_ID))
                    .thenThrow(com.ecommerce.customer.application.exceptions.CustomerNotFoundException.byId(CUSTOMER_ID.toString()));

            mockMvc.perform(get("/api/customers/me/membership")
                            .header("X-Customer-Id", CUSTOMER_ID.toString()))
                    .andExpect(status().isNotFound());
        }
    }
}
