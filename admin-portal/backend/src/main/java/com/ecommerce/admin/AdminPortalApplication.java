package com.ecommerce.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Admin Portal.
 */
@SpringBootApplication(scanBasePackages = {
        "com.ecommerce.admin",
        "com.ecommerce.security"
})
public class AdminPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminPortalApplication.class, args);
    }
}
