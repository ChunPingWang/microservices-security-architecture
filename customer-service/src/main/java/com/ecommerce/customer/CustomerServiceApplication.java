package com.ecommerce.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Customer Service.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.ecommerce.customer",
        "com.ecommerce.security"
})
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
