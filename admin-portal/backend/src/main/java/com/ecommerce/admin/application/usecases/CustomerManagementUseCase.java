package com.ecommerce.admin.application.usecases;

import com.ecommerce.admin.application.dto.CustomerSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Use case for customer management operations.
 * Note: In a real implementation, this would call the customer-service via Feign client.
 */
@Service
public class CustomerManagementUseCase {

    // Mock data store for demo purposes
    private final Map<UUID, CustomerSummary> customers = new ConcurrentHashMap<>();

    public CustomerManagementUseCase() {
        // Initialize with some mock data
        UUID customer1 = UUID.randomUUID();
        UUID customer2 = UUID.randomUUID();
        UUID customer3 = UUID.randomUUID();

        customers.put(customer1, new CustomerSummary(customer1, "user1@example.com", "王小明",
                "GOLD", new BigDecimal("50000"), true, LocalDateTime.now().minusMonths(6)));
        customers.put(customer2, new CustomerSummary(customer2, "user2@example.com", "李小華",
                "SILVER", new BigDecimal("15000"), true, LocalDateTime.now().minusMonths(3)));
        customers.put(customer3, new CustomerSummary(customer3, "vip@example.com", "張大戶",
                "PLATINUM", new BigDecimal("200000"), true, LocalDateTime.now().minusYears(1)));
    }

    /**
     * Lists all customers.
     */
    public List<CustomerSummary> listCustomers(UUID adminId) {
        return new ArrayList<>(customers.values());
    }

    /**
     * Lists customers by member level.
     */
    public List<CustomerSummary> listCustomersByMemberLevel(UUID adminId, String memberLevel) {
        return customers.values().stream()
                .filter(c -> c.memberLevel().equals(memberLevel))
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific customer.
     */
    public CustomerSummary getCustomer(UUID adminId, UUID customerId) {
        return customers.get(customerId);
    }

    /**
     * Toggles customer account status.
     */
    public boolean toggleCustomerStatus(UUID adminId, UUID customerId, Boolean active) {
        CustomerSummary existing = customers.get(customerId);
        if (existing == null) {
            return false;
        }

        CustomerSummary updated = new CustomerSummary(
                existing.customerId(),
                existing.email(),
                existing.name(),
                existing.memberLevel(),
                existing.totalSpending(),
                active,
                existing.createdAt()
        );
        customers.put(customerId, updated);
        return true;
    }

    /**
     * Updates customer member level.
     */
    public boolean updateMemberLevel(UUID adminId, UUID customerId, String memberLevel) {
        CustomerSummary existing = customers.get(customerId);
        if (existing == null) {
            return false;
        }

        CustomerSummary updated = new CustomerSummary(
                existing.customerId(),
                existing.email(),
                existing.name(),
                memberLevel,
                existing.totalSpending(),
                existing.active(),
                existing.createdAt()
        );
        customers.put(customerId, updated);
        return true;
    }
}
