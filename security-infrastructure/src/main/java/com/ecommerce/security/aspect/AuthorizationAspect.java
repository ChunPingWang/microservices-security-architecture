package com.ecommerce.security.aspect;

import com.ecommerce.security.annotation.RequirePermission;
import com.ecommerce.security.context.CurrentUserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect for enforcing permission-based authorization.
 * Intercepts methods annotated with @RequirePermission.
 */
@Aspect
@Component
public class AuthorizationAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationAspect.class);

    private final CurrentUserContext currentUserContext;

    public AuthorizationAspect(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    @Around("@annotation(com.ecommerce.security.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        RequirePermission annotation = getAnnotation(joinPoint);
        String[] requiredPermissions = annotation.value();
        boolean requireAll = annotation.requireAll();

        if (!currentUserContext.isAuthenticated()) {
            log.warn("Access denied: user not authenticated");
            throw new AccessDeniedException("Authentication required");
        }

        String userRoles = currentUserContext.getRoles().orElse("");
        boolean hasPermission = checkUserPermissions(
            userRoles,
            requiredPermissions,
            requireAll
        );

        if (!hasPermission) {
            log.warn("Access denied for user {}: required permissions {}",
                currentUserContext.getUserId().orElse("unknown"),
                Arrays.toString(requiredPermissions)
            );
            throw new AccessDeniedException("Insufficient permissions");
        }

        log.debug("Permission granted for user {} to access {}",
            currentUserContext.getUserId().orElse("unknown"),
            joinPoint.getSignature().getName()
        );

        return joinPoint.proceed();
    }

    private RequirePermission getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(RequirePermission.class);
    }

    private boolean checkUserPermissions(
            String userRoles,
            String[] requiredPermissions,
            boolean requireAll
    ) {
        if (requiredPermissions.length == 0) {
            return true;
        }

        if (requireAll) {
            return Arrays.stream(requiredPermissions)
                .allMatch(p -> userRoles.contains(p));
        } else {
            return Arrays.stream(requiredPermissions)
                .anyMatch(p -> userRoles.contains(p));
        }
    }
}
