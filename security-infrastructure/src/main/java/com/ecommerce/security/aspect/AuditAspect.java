package com.ecommerce.security.aspect;

import com.ecommerce.security.context.CurrentUserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Aspect for audit logging of sensitive operations.
 * Logs user actions for security compliance and troubleshooting.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final CurrentUserContext currentUserContext;

    public AuditAspect(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    /**
     * Audit all controller methods in web package.
     */
    @Around("execution(* com.ecommerce..infrastructure.web..*Controller.*(..))")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditOperation(joinPoint, "API");
    }

    /**
     * Audit all use case methods.
     */
    @Around("execution(* com.ecommerce..application.use_cases..*UseCase.*(..))")
    public Object auditUseCaseMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditOperation(joinPoint, "USE_CASE");
    }

    private Object auditOperation(
            ProceedingJoinPoint joinPoint,
            String operationType
    ) throws Throwable {
        String userId = currentUserContext.getUserId().orElse("anonymous");
        String methodName = getMethodName(joinPoint);
        Instant startTime = Instant.now();

        try {
            Object result = joinPoint.proceed();
            logSuccess(operationType, userId, methodName, startTime);
            return result;
        } catch (Exception e) {
            logFailure(operationType, userId, methodName, startTime, e);
            throw e;
        }
    }

    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() +
               "." +
               signature.getName();
    }

    private void logSuccess(
            String type,
            String userId,
            String method,
            Instant startTime
    ) {
        long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        auditLog.info(
            "AUDIT|type={}|user={}|method={}|status=SUCCESS|duration={}ms",
            type, userId, method, duration
        );
    }

    private void logFailure(
            String type,
            String userId,
            String method,
            Instant startTime,
            Exception e
    ) {
        long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        auditLog.warn(
            "AUDIT|type={}|user={}|method={}|status=FAILURE|duration={}ms|error={}",
            type, userId, method, duration, e.getMessage()
        );
    }
}
