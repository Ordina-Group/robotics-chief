import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val vertxVersion = "4.4.5"
val coroutinesVersion = "1.7.3"
val kotlin_logging_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"

    application
}

group = "nl.ordina.robotics"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("nl.ordina.robotics.server.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-config")
    implementation("io.vertx:vertx-stomp")
    implementation("io.vertx:vertx-opentelemetry")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")

    implementation(platform("io.opentelemetry:opentelemetry-bom:1.31.0"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")

    implementation("org.apache.commons:commons-configuration2:2.9.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.slf4j:jul-to-slf4j:2.0.9")
    implementation("io.github.oshai:kotlin-logging:$kotlin_logging_version")

    implementation("net.harawata:appdirs:1.2.1")

    implementation("org.apache.sshd:sshd-core:2.9.2")
    implementation("org.apache.sshd:sshd-common:2.9.2")
    implementation("org.apache.sshd:sshd-contrib:2.9.2")
    implementation("org.apache.sshd:sshd-sftp:2.9.2")
    implementation("net.i2p.crypto:eddsa:0.3.0")

    implementation(project(":frontend"))

    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets["main"].resources {
    srcDir(
        project.projectDir.toPath()
            .resolve("..").resolve("frontend").resolve("build")
            .toFile(),
    )
}
