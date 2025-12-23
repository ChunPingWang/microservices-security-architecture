plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("spotbugs-conventions")
}

description = "Customer management microservice"

dependencies {
    implementation(project(":shared-kernel"))
    implementation(project(":security-infrastructure"))

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Redis for session
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Flyway
    implementation("org.flywaydb:flyway-core:10.4.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.4.1")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Password encoding
    implementation("org.springframework.security:spring-security-crypto")

    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("com.h2database:h2")
}
