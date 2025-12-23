plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.6")
}
