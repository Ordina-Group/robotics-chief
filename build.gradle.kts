import com.github.gradle.node.npm.task.NpmTask

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
    id("com.github.node-gradle.node") version "4.0.0"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
}

group = "nl.ordina.robotics"
version = "0.0.1"
application {
    mainClass.set("nl.ordina.robotics.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.sshd:sshd-core:2.9.2")
    implementation("org.apache.sshd:sshd-common:2.9.2")
    implementation("org.apache.sshd:sshd-contrib:2.9.2")
    implementation("org.apache.sshd:sshd-sftp:2.9.2")

    implementation("net.harawata:appdirs:1.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")

    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-compression-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

sourceSets["main"].resources {
    srcDir(project.projectDir.toPath().resolve("frontend").resolve("build").toFile())
}

tasks.named("classes") {
    dependsOn("buildFrontend")
}

node {
    version.set("18.16.0")
    nodeProjectDir.set(project.projectDir.toPath().resolve("frontend").toFile())
    download.set(true)
}

task<NpmTask>("buildFrontend") {
    group = "build"
    dependsOn.add("npmInstall")

    args.addAll("run", "build")

    inputs.files(fileTree("frontend/src"))
    inputs.files("frontend/package.json")
    inputs.files("frontend/package-lock.json")
    inputs.files("frontend/svelte.config.js")
    inputs.files("frontend/tsconfig.json")

    outputs.dir("frontend/build")
}
