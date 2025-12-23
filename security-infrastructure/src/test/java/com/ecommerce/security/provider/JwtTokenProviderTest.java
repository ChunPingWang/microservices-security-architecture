package com.ecommerce.security.provider;

import com.ecommerce.security.config.JwtConfig;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider.
 */
@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtConfig.setSecretKey("test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha");
        jwtConfig.setAccessTokenExpirationMs(900000); // 15 minutes
        jwtConfig.setRefreshTokenExpirationMs(604800000); // 7 days
        jwtConfig.setIssuer("test-issuer");
        jwtConfig.setAudience("test-audience");

        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
    }

    @Nested
    @DisplayName("Access Token Generation")
    class AccessTokenGeneration {

        @Test
        @DisplayName("should generate valid access token")
        void shouldGenerateValidAccessToken() {
            String token = jwtTokenProvider.generateAccessToken(
                "user-123",
                "test@example.com",
                "CUSTOMER"
            );

            assertThat(token).isNotBlank();
            assertThat(jwtTokenProvider.validateToken(token)).isPresent();
        }

        @Test
        @DisplayName("should include user claims in token")
        void shouldIncludeUserClaimsInToken() {
            String userId = "user-123";
            String email = "test@example.com";
            String roles = "CUSTOMER,VIP";

            String token = jwtTokenProvider.generateAccessToken(userId, email, roles);
            Optional<Claims> claims = jwtTokenProvider.validateToken(token);

            assertThat(claims).isPresent();
            assertThat(claims.get().get("userId", String.class)).isEqualTo(userId);
            assertThat(claims.get().get("email", String.class)).isEqualTo(email);
            assertThat(claims.get().get("roles", String.class)).isEqualTo(roles);
        }
    }

    @Nested
    @DisplayName("Refresh Token Generation")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("user-123");

            assertThat(token).isNotBlank();
            assertThat(jwtTokenProvider.validateToken(token)).isPresent();
        }

        @Test
        @DisplayName("should include user ID in refresh token")
        void shouldIncludeUserIdInRefreshToken() {
            String userId = "user-456";
            String token = jwtTokenProvider.generateRefreshToken(userId);

            Optional<String> extractedUserId = jwtTokenProvider.getUserId(token);
            assertThat(extractedUserId).contains(userId);
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("should return empty for invalid token")
        void shouldReturnEmptyForInvalidToken() {
            Optional<Claims> claims = jwtTokenProvider.validateToken("invalid-token");
            assertThat(claims).isEmpty();
        }

        @Test
        @DisplayName("should return empty for tampered token")
        void shouldReturnEmptyForTamperedToken() {
            String token = jwtTokenProvider.generateAccessToken(
                "user-123",
                "test@example.com",
                "CUSTOMER"
            );

            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";
            Optional<Claims> claims = jwtTokenProvider.validateToken(tamperedToken);

            assertThat(claims).isEmpty();
        }

        @Test
        @DisplayName("should detect expired token")
        void shouldDetectExpiredToken() {
            // Create config with very short expiration
            JwtConfig shortExpiryConfig = new JwtConfig();
            shortExpiryConfig.setSecretKey(
                "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha"
            );
            shortExpiryConfig.setAccessTokenExpirationMs(1); // 1ms expiration

            JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(shortExpiryConfig);
            String token = shortExpiryProvider.generateAccessToken(
                "user-123",
                "test@example.com",
                "CUSTOMER"
            );

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(shortExpiryProvider.isTokenExpired(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("Claim Extraction")
    class ClaimExtraction {

        @Test
        @DisplayName("should extract user ID from token")
        void shouldExtractUserIdFromToken() {
            String userId = "user-789";
            String token = jwtTokenProvider.generateAccessToken(
                userId,
                "test@example.com",
                "CUSTOMER"
            );

            assertThat(jwtTokenProvider.getUserId(token)).contains(userId);
        }

        @Test
        @DisplayName("should extract email from token")
        void shouldExtractEmailFromToken() {
            String email = "user@example.com";
            String token = jwtTokenProvider.generateAccessToken(
                "user-123",
                email,
                "CUSTOMER"
            );

            assertThat(jwtTokenProvider.getEmail(token)).contains(email);
        }

        @Test
        @DisplayName("should extract roles from token")
        void shouldExtractRolesFromToken() {
            String roles = "ADMIN,SUPER_ADMIN";
            String token = jwtTokenProvider.generateAccessToken(
                "user-123",
                "admin@example.com",
                roles
            );

            assertThat(jwtTokenProvider.getRoles(token)).contains(roles);
        }
    }
}
