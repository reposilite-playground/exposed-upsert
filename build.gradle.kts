import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.dzikoysk"
version = "1.0.0"

plugins {
    kotlin("jvm") version "1.5.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    val exposed = "0.32.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposed")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed")
    testImplementation("com.h2database:h2:1.4.199")

    testImplementation("mysql:mysql-connector-java:8.0.25")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:2.7.3")

    val testcontainers = "1.15.3"
    testImplementation("org.testcontainers:postgresql:$testcontainers")
    testImplementation("org.testcontainers:oracle-xe:$testcontainers")
    testImplementation("org.testcontainers:mariadb:$testcontainers")
    testImplementation("org.testcontainers:mysql:$testcontainers")
    testImplementation("org.testcontainers:testcontainers:$testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    val logback = "1.2.3"
    testImplementation("ch.qos.logback:logback-core:$logback")
    testImplementation("ch.qos.logback:logback-classic:$logback")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}