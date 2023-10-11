plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("com.github.node-gradle.node") version "4.0.0"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

group = "nl.ordina.robotics"
version = "0.0.1"

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.48.2")
}
