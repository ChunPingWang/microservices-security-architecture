package com.ecommerce.security.filter;

import com.ecommerce.security.context.CurrentUserContext;
import com.ecommerce.security.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JWT authentication filter that extracts and validates JWT tokens.
 * Sets up Spring Security context for authenticated requests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserContext currentUserContext;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CurrentUserContext currentUserContext
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.currentUserContext = currentUserContext;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            extractToken(request).ifPresent(this::authenticateToken);
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return Optional.of(bearerToken.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    private void authenticateToken(String token) {
        Optional<Claims> claimsOpt = jwtTokenProvider.validateToken(token);

        claimsOpt.ifPresent(claims -> {
            String userId = claims.get("userId", String.class);
            String email = claims.get("email", String.class);
            String rolesStr = claims.get("roles", String.class);

            List<SimpleGrantedAuthority> authorities = parseRoles(rolesStr);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Set current user context
            currentUserContext.setCurrentUser(userId, email, rolesStr);
        });
    }

    private List<SimpleGrantedAuthority> parseRoles(String rolesStr) {
        if (!StringUtils.hasText(rolesStr)) {
            return List.of();
        }
        return Arrays.stream(rolesStr.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }
}
