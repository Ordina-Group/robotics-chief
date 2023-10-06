// val ktor_version: String by project
// val kotlin_logging_version: String by project
// val kotlin_version: String by project
// val logback_version: String by project
// val slf4j_version: String by project

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
    id("com.github.node-gradle.node") version "4.0.0"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

group = "nl.ordina.robotics"
version = "0.0.1"
application {
    mainClass.set("nl.ordina.robotics.ApplicationKt")
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.48.2")
}

// dependencies {
//    implementation("org.apache.sshd:sshd-core:2.9.2")
//    implementation("org.apache.sshd:sshd-common:2.9.2")
//    implementation("org.apache.sshd:sshd-contrib:2.9.2")
//    implementation("org.apache.sshd:sshd-sftp:2.9.2")
//
//    implementation("net.harawata:appdirs:1.2.1")
//
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
//
//    // https://central.sonatype.com/artifact/org.slf4j/slf4j-api/
//    implementation("org.slf4j:slf4j-api:$slf4j_version")
//    // https://package-search.jetbrains.com/package?id=ch.qos.logback%3Alogback-classic&tab=versions
//    implementation("ch.qos.logback:logback-classic:$logback_version")
//    // https://github.com/oshai/kotlin-logging/releases
//    implementation("io.github.microutils:kotlin-logging:$kotlin_logging_version")
//
//    // https://ktor.io/changelog/
//    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
//    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-openapi:$ktor_version")
//    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-compression-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-resources:$ktor_version")
//    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-websockets:$ktor_version")
//
//    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
//    testImplementation("io.ktor:ktor-client-websockets:$ktor_version")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
// }
