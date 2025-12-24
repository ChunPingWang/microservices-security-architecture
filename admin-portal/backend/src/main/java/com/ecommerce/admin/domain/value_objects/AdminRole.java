package com.ecommerce.admin.domain.value_objects;

import java.util.Set;

/**
 * Admin role enumeration with associated permissions.
 */
public enum AdminRole {
    SUPER_ADMIN(Set.of(
            "MANAGE_PRODUCTS",
            "MANAGE_ORDERS",
            "MANAGE_CUSTOMERS",
            "MANAGE_PROMOTIONS",
            "VIEW_REPORTS",
            "MANAGE_ADMINS"
    )),
    PRODUCT_MANAGER(Set.of(
            "MANAGE_PRODUCTS",
            "VIEW_REPORTS"
    )),
    ORDER_MANAGER(Set.of(
            "MANAGE_ORDERS",
            "VIEW_REPORTS"
    )),
    CUSTOMER_SERVICE(Set.of(
            "MANAGE_CUSTOMERS",
            "VIEW_REPORTS"
    )),
    MARKETING(Set.of(
            "MANAGE_PROMOTIONS",
            "VIEW_REPORTS"
    ));

    private final Set<String> permissions;

    AdminRole(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
