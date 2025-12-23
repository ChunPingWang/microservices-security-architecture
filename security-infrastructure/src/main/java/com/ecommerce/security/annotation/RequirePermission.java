package com.ecommerce.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce permission checks on methods.
 * Used by AuthorizationAspect to validate user permissions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * Required permission(s) - user must have at least one.
     */
    String[] value();

    /**
     * If true, user must have ALL specified permissions.
     * If false (default), user needs at least one permission.
     */
    boolean requireAll() default false;

    /**
     * Optional resource type for resource-level permissions.
     */
    String resource() default "";
}
