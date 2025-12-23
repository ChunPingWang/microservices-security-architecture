package com.ecommerce.gateway.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Request logging filter for all incoming requests.
 * Logs request details and response status for monitoring.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = generateRequestId(exchange);
        long startTime = Instant.now().toEpochMilli();

        ServerHttpRequest request = exchange.getRequest();
        logRequest(requestId, request);

        // Add request ID to response headers
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> logResponse(
                requestId,
                exchange.getResponse(),
                startTime
            )));
    }

    private String generateRequestId(ServerWebExchange exchange) {
        String existingId = exchange.getRequest()
            .getHeaders()
            .getFirst(REQUEST_ID_HEADER);

        if (existingId != null && !existingId.isEmpty()) {
            return existingId;
        }

        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void logRequest(String requestId, ServerHttpRequest request) {
        log.info(
            "REQUEST|id={}|method={}|path={}|client={}|userAgent={}",
            requestId,
            request.getMethod(),
            request.getPath(),
            getClientIp(request),
            request.getHeaders().getFirst("User-Agent")
        );
    }

    private void logResponse(
            String requestId,
            ServerHttpResponse response,
            long startTime
    ) {
        long duration = Instant.now().toEpochMilli() - startTime;
        log.info(
            "RESPONSE|id={}|status={}|duration={}ms",
            requestId,
            response.getStatusCode(),
            duration
        );
    }

    private String getClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        var remoteAddress = request.getRemoteAddress();
        return remoteAddress != null
            ? remoteAddress.getAddress().getHostAddress()
            : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
