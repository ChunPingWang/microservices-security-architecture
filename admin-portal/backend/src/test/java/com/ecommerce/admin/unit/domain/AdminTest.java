package com.ecommerce.admin.unit.domain;

import com.ecommerce.admin.domain.entities.Admin;
import com.ecommerce.admin.domain.value_objects.AdminRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Admin entity.
 */
@DisplayName("Admin Entity")
class AdminTest {

    @Nested
    @DisplayName("Admin Creation")
    class AdminCreation {

        @Test
        @DisplayName("should create admin with valid data")
        void shouldCreateAdminWithValidData() {
            Admin admin = Admin.create(
                    "admin@ecommerce.com",
                    "SecurePass123",
                    "系統管理員",
                    AdminRole.SUPER_ADMIN
            );

            assertNotNull(admin.getId());
            assertEquals("admin@ecommerce.com", admin.getEmail());
            assertEquals("系統管理員", admin.getName());
            assertEquals(AdminRole.SUPER_ADMIN, admin.getRole());
            assertTrue(admin.isActive());
        }

        @Test
        @DisplayName("should create product manager admin")
        void shouldCreateProductManagerAdmin() {
            Admin admin = Admin.create(
                    "product@ecommerce.com",
                    "SecurePass123",
                    "商品管理員",
                    AdminRole.PRODUCT_MANAGER
            );

            assertEquals(AdminRole.PRODUCT_MANAGER, admin.getRole());
        }

        @Test
        @DisplayName("should create order manager admin")
        void shouldCreateOrderManagerAdmin() {
            Admin admin = Admin.create(
                    "order@ecommerce.com",
                    "SecurePass123",
                    "訂單管理員",
                    AdminRole.ORDER_MANAGER
            );

            assertEquals(AdminRole.ORDER_MANAGER, admin.getRole());
        }

        @Test
        @DisplayName("should fail when email is null")
        void shouldFailWhenEmailIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Admin.create(null, "SecurePass123", "Admin", AdminRole.SUPER_ADMIN));
        }

        @Test
        @DisplayName("should fail when password is null")
        void shouldFailWhenPasswordIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Admin.create("admin@test.com", null, "Admin", AdminRole.SUPER_ADMIN));
        }
    }

    @Nested
    @DisplayName("Admin Authentication")
    class AdminAuthentication {

        @Test
        @DisplayName("should authenticate with correct password")
        void shouldAuthenticateWithCorrectPassword() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.SUPER_ADMIN
            );

            assertTrue(admin.authenticate("SecurePass123"));
        }

        @Test
        @DisplayName("should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.SUPER_ADMIN
            );

            assertFalse(admin.authenticate("WrongPassword"));
        }

        @Test
        @DisplayName("should not authenticate when deactivated")
        void shouldNotAuthenticateWhenDeactivated() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.SUPER_ADMIN
            );
            admin.deactivate();

            assertFalse(admin.authenticate("SecurePass123"));
        }
    }

    @Nested
    @DisplayName("Admin Status")
    class AdminStatus {

        @Test
        @DisplayName("should deactivate admin")
        void shouldDeactivateAdmin() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.SUPER_ADMIN
            );
            admin.deactivate();

            assertFalse(admin.isActive());
        }

        @Test
        @DisplayName("should reactivate admin")
        void shouldReactivateAdmin() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.SUPER_ADMIN
            );
            admin.deactivate();
            admin.activate();

            assertTrue(admin.isActive());
        }
    }

    @Nested
    @DisplayName("Admin Role Permissions")
    class AdminRolePermissions {

        @Test
        @DisplayName("super admin should have all permissions")
        void superAdminShouldHaveAllPermissions() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.SUPER_ADMIN
            );

            assertTrue(admin.hasPermission("MANAGE_PRODUCTS"));
            assertTrue(admin.hasPermission("MANAGE_ORDERS"));
            assertTrue(admin.hasPermission("MANAGE_CUSTOMERS"));
            assertTrue(admin.hasPermission("MANAGE_PROMOTIONS"));
            assertTrue(admin.hasPermission("VIEW_REPORTS"));
            assertTrue(admin.hasPermission("MANAGE_ADMINS"));
        }

        @Test
        @DisplayName("product manager should only have product permissions")
        void productManagerShouldOnlyHaveProductPermissions() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.PRODUCT_MANAGER
            );

            assertTrue(admin.hasPermission("MANAGE_PRODUCTS"));
            assertFalse(admin.hasPermission("MANAGE_ORDERS"));
            assertFalse(admin.hasPermission("MANAGE_ADMINS"));
        }

        @Test
        @DisplayName("order manager should only have order permissions")
        void orderManagerShouldOnlyHaveOrderPermissions() {
            Admin admin = Admin.create(
                    "admin@test.com",
                    "SecurePass123",
                    "Admin",
                    AdminRole.ORDER_MANAGER
            );

            assertTrue(admin.hasPermission("MANAGE_ORDERS"));
            assertTrue(admin.hasPermission("VIEW_REPORTS"));
            assertFalse(admin.hasPermission("MANAGE_PRODUCTS"));
            assertFalse(admin.hasPermission("MANAGE_ADMINS"));
        }
    }
}
