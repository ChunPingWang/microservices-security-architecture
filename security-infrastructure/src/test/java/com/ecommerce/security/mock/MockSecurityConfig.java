package com.ecommerce.security.mock;

import com.ecommerce.security.context.CurrentUserContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Mock security configuration for testing.
 * Disables security and provides mock user context.
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class MockSecurityConfig {

    /**
     * Permits all requests for testing.
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }

    /**
     * Mock current user context for testing.
     */
    @Bean
    @Primary
    @RequestScope
    public CurrentUserContext mockCurrentUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setCurrentUser("test-user-id", "test@example.com", "CUSTOMER");
        return context;
    }

    /**
     * Helper to create a mock context with custom values.
     */
    public static CurrentUserContext createMockContext(
            String userId,
            String email,
            String roles
    ) {
        CurrentUserContext context = new CurrentUserContext();
        context.setCurrentUser(userId, email, roles);
        return context;
    }

    /**
     * Helper to create an admin mock context.
     */
    public static CurrentUserContext createAdminContext() {
        return createMockContext("admin-id", "admin@example.com", "ADMIN,SUPER_ADMIN");
    }

    /**
     * Helper to create a customer mock context.
     */
    public static CurrentUserContext createCustomerContext(String customerId) {
        return createMockContext(customerId, "customer@example.com", "CUSTOMER");
    }
}
