plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("org.springframework.boot") version "3.2.1" apply false
    id("checkstyle")
}

allprojects {
    group = "com.ecommerce"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    checkstyle {
        toolVersion = "10.12.5"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isIgnoreFailures = false
        maxWarnings = 0
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.assertj:assertj-core:3.24.2")
        testImplementation("org.mockito:mockito-core:5.8.0")
        testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    }
}

// Configure Spring Boot services
configure(subprojects.filter {
    it.name.endsWith("-service") ||
    it.name == "api-gateway" ||
    it.name == "backend"
}) {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "spotbugs-conventions")

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.testcontainers:junit-jupiter:1.19.3")
    }
}
