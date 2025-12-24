package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Order Service.
 * Handles shopping cart and order management.
 */
@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "com.ecommerce.order",
        "com.ecommerce.security"
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
