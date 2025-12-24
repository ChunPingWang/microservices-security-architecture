plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("spotbugs-conventions")
}

description = "Sales, promotions and coupons microservice"

dependencies {
    implementation(project(":shared-kernel"))
    implementation(project(":security-infrastructure"))

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.flywaydb:flyway-core:10.4.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.4.1")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // OpenFeign for service-to-service calls
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0")

    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("com.h2database:h2")
}
