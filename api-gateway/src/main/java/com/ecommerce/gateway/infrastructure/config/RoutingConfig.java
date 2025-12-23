package com.ecommerce.gateway.infrastructure.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration.
 * Defines routes to all microservices.
 */
@Configuration
public class RoutingConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Customer Service routes
            .route("customer-service", r -> r
                .path("/api/customers/**", "/api/auth/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .circuitBreaker(c -> c.setName("customerService")
                        .setFallbackUri("forward:/fallback/customer"))
                )
                .uri("lb://customer-service"))

            // Product Service routes
            .route("product-service", r -> r
                .path("/api/products/**", "/api/categories/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .circuitBreaker(c -> c.setName("productService")
                        .setFallbackUri("forward:/fallback/product"))
                )
                .uri("lb://product-service"))

            // Order Service routes
            .route("order-service", r -> r
                .path("/api/orders/**", "/api/cart/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .circuitBreaker(c -> c.setName("orderService")
                        .setFallbackUri("forward:/fallback/order"))
                )
                .uri("lb://order-service"))

            // Payment Service routes
            .route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .circuitBreaker(c -> c.setName("paymentService")
                        .setFallbackUri("forward:/fallback/payment"))
                )
                .uri("lb://payment-service"))

            // Logistics Service routes
            .route("logistics-service", r -> r
                .path("/api/shipments/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .circuitBreaker(c -> c.setName("logisticsService")
                        .setFallbackUri("forward:/fallback/logistics"))
                )
                .uri("lb://logistics-service"))

            // Sales Service routes
            .route("sales-service", r -> r
                .path("/api/promotions/**", "/api/coupons/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .circuitBreaker(c -> c.setName("salesService")
                        .setFallbackUri("forward:/fallback/sales"))
                )
                .uri("lb://sales-service"))

            .build();
    }
}
