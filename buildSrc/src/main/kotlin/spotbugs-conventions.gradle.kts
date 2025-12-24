import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("com.github.spotbugs")
}

spotbugs {
    // Set to true during development, should be false for production
    ignoreFailures.set(true)
    showProgress.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
}

tasks.withType<SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
        outputLocation.set(file("${project.layout.buildDirectory.get()}/reports/spotbugs/${name}.html"))
    }
    reports.create("xml") {
        required.set(false)
    }
}
