plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

description = "Shared security infrastructure module for authentication and authorization"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.1")
    }
}

dependencies {
    api(project(":shared-kernel"))

    // Spring Web for servlet classes
    api("org.springframework.boot:spring-boot-starter-web")

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.security:spring-security-oauth2-jose")

    // JWT
    api("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // AOP for authorization aspects
    api("org.springframework.boot:spring-boot-starter-aop")

    // Feign for service-to-service auth
    api("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0")

    // Distributed Tracing
    api("io.micrometer:micrometer-tracing-bridge-brave")
    api("io.zipkin.reporter2:zipkin-reporter-brave")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
