import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

// REMOVED: repositories { mavenCentral() } block is deleted from here

dependencies {
    // Core library containing launch, async, Channels, and Flows
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // Optional: Utilities specifically for writing unit tests for your coroutines/flows
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}
