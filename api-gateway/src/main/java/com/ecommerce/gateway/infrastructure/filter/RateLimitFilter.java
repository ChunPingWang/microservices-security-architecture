package com.ecommerce.gateway.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Rate limiting filter using Redis.
 * Limits requests per IP address.
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final ReactiveStringRedisTemplate redisTemplate;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = getClientIp(exchange);
        String key = RATE_LIMIT_PREFIX + clientIp;

        return redisTemplate.opsForValue()
            .increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    // First request, set expiration
                    return redisTemplate.expire(key, WINDOW_DURATION)
                        .then(chain.filter(exchange));
                }

                if (count > MAX_REQUESTS_PER_MINUTE) {
                    log.warn("Rate limit exceeded for IP: {}", clientIp);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }

                return chain.filter(exchange);
            })
            .onErrorResume(e -> {
                // If Redis is unavailable, allow request
                log.error("Rate limiting error: {}", e.getMessage());
                return chain.filter(exchange);
            });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest()
            .getHeaders()
            .getFirst("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null
            ? remoteAddress.getAddress().getHostAddress()
            : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
