package com.ecommerce.security.provider;

import com.ecommerce.security.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * JWT token provider for generating and validating JWT tokens.
 * Handles access token and refresh token lifecycle.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(
            jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Generates an access token for the given user.
     */
    public String generateAccessToken(String userId, String email, String roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpirationMs());

        return Jwts.builder()
            .subject(userId)
            .issuer(jwtConfig.getIssuer())
            .audience().add(jwtConfig.getAudience()).and()
            .issuedAt(now)
            .expiration(expiry)
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_EMAIL, email)
            .claim(CLAIM_ROLES, roles)
            .signWith(secretKey)
            .compact();
    }

    /**
     * Generates a refresh token for the given user.
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpirationMs());

        return Jwts.builder()
            .subject(userId)
            .issuer(jwtConfig.getIssuer())
            .issuedAt(now)
            .expiration(expiry)
            .claim(CLAIM_USER_ID, userId)
            .signWith(secretKey)
            .compact();
    }

    /**
     * Validates the token and returns claims if valid.
     */
    public Optional<Claims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts user ID from the token.
     */
    public Optional<String> getUserId(String token) {
        return validateToken(token)
            .map(claims -> claims.get(CLAIM_USER_ID, String.class));
    }

    /**
     * Extracts email from the token.
     */
    public Optional<String> getEmail(String token) {
        return validateToken(token)
            .map(claims -> claims.get(CLAIM_EMAIL, String.class));
    }

    /**
     * Extracts roles from the token.
     */
    public Optional<String> getRoles(String token) {
        return validateToken(token)
            .map(claims -> claims.get(CLAIM_ROLES, String.class));
    }

    /**
     * Checks if the token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }
}
