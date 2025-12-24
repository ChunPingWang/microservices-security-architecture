package com.ecommerce.security.interceptor;

import com.ecommerce.security.provider.JwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Feign request interceptor for service-to-service authentication.
 * Adds JWT token to outgoing service requests.
 */
@Component
public class ServiceAuthInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SERVICE_USER_ID = "service-internal";
    private static final String SERVICE_EMAIL = "service@internal";
    private static final String SERVICE_ROLE = "SERVICE";

    private final JwtTokenProvider jwtTokenProvider;

    public ServiceAuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        // Generate service-to-service token
        String serviceToken = jwtTokenProvider.generateAccessToken(
            SERVICE_USER_ID,
            SERVICE_EMAIL,
            SERVICE_ROLE
        );

        template.header(AUTHORIZATION_HEADER, BEARER_PREFIX + serviceToken);
    }
}
