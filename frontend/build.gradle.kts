import com.github.gradle.node.npm.task.NpmTask

plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.node-gradle.node") version "4.0.0"
}

group = "nl.ordina.robotics"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

node {
    version.set("18.16.0")
    nodeProjectDir.set(project.projectDir)
    download.set(true)
}

tasks.named("build") {
    dependsOn("buildFrontend")
}

task<NpmTask>("buildFrontend") {
    group = "build"
    dependsOn.add("npmInstall")

    args.addAll("run", "build")

    inputs.files(fileTree("src"))
    inputs.files("package.json")
    inputs.files("package-lock.json")
    inputs.files("svelte.config.js")
    inputs.files("tsconfig.json")

    outputs.dir("build")
}

task<NpmTask>("liveFrontend") {
    group = "application"
    dependsOn.add("npmInstall")

    args.addAll("run", "dev")

    inputs.files(fileTree("src"))
    inputs.files("package.json")
    inputs.files("package-lock.json")
    inputs.files("svelte.config.js")
    inputs.files("tsconfig.json")

    outputs.dir("build/resources/static")
}
