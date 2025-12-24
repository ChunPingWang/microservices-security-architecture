plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("spotbugs-conventions")
}

description = "API Gateway for routing and security"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
    }
}

dependencies {
    implementation(project(":security-infrastructure"))

    // Spring Cloud Gateway
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // Resilience4j for circuit breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // Redis for rate limiting
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Actuator for health checks
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Micrometer for metrics
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
