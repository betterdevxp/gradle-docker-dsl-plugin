plugins {
    id("idea")
    id("groovy")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.bmuschko:gradle-docker-plugin:6.7.0")
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

val pluginDescription = "Gradle plugin for managing Docker containers, specifically in the context of the local and CI build/test lifecycle. " +
        "This plugin provides a handy DSL for the excellent https://github.com/bmuschko/gradle-docker-plugin."

pluginBundle {
    website = "https://github.com/betterdevxp/gradle-docker-dsl-plugin"
    vcsUrl = "https://github.com/betterdevxp/gradle-docker-dsl-plugin.git"
    tags = listOf("gradle", "docker", "testing")
    description = pluginDescription
}

gradlePlugin {
    plugins {
        create("dockerDsl") {
            id = "org.betterdevxp.docker-dsl"
            displayName = "Docker DSL"
            description = pluginDescription
            implementationClass = "org.betterdevxp.dockerdsl.DockerDslPlugin"
        }
    }
}

sourceSets {
    create("functionalTest")
}

gradlePlugin {
    testSourceSets(sourceSets["functionalTest"])
}

configurations {
    getByName("functionalTestImplementation").extendsFrom(configurations["testImplementation"])
}

tasks.register<Test>("functionalTest") {
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
}

tasks.named("check") {
    dependsOn(tasks.named("functionalTest"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}