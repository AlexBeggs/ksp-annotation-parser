import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    java
    kotlin("jvm") version "1.5.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    jacoco
    pmd
    id("io.gitlab.arturbosch.detekt").version("1.17.1")
}

group = "io.github.alexbeggs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test/kotlin")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0-beta08")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.2")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.2")
    testImplementation("com.google.truth:truth:1.1.3")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.21.0"
    rulesMinimumPriority.set(5)
    ruleSets = listOf("category/java/errorprone.xml", "category/java/bestpractices.xml")
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins
        txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
        sarif.enabled = true // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
    filter {
        exclude("**/style-violations.kt")
    }
}
