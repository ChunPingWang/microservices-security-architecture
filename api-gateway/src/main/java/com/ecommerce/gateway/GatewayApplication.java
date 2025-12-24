package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway application entry point.
 * Routes requests to appropriate microservices.
 */
@SpringBootApplication(scanBasePackages = {
    "com.ecommerce.gateway",
    "com.ecommerce.security"
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
