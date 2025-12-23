plugins {
    id("java-library")
}

description = "Shared domain objects and events for cross-service use"

dependencies {
    // No Spring dependencies - pure domain objects
    api("jakarta.validation:jakarta.validation-api:3.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.assertj:assertj-core:3.24.2")

    // Dependencies for AbstractIntegrationTest base class
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.1")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
}
