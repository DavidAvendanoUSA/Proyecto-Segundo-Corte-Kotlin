plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.11"
val logbackVersion = "1.4.14"
val exposedVersion = "0.48.0"
val hikariVersion = "5.1.0"
val sqliteVersion = "3.45.3.0"
val flywayVersion = "9.22.3" // usamos 9.x para evitar el repo de Flyway

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // DB
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")

    // Flyway 9.x (solo core en Maven Central)
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
}

kotlin { jvmToolchain(17) }

application {
    mainClass.set("com.example.ApplicationKt")
}

tasks.test { useJUnitPlatform() }