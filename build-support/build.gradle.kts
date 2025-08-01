/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.jvm)
}

group = "aws.sdk.kotlin"

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        val kotlin_api_version: String? by project
        val kotlin_language_version: String? by project

        val apiVersionToUse = kotlin_api_version ?: "2.0"
        val languageVersionToUse = kotlin_language_version ?: "2.0"

        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.fromVersion(languageVersionToUse))
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.fromVersion(apiVersionToUse))
    }
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))

    implementation(libs.smithy.model)
    implementation(libs.smithy.aws.traits)
    implementation(libs.smithy.protocol.traits)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.smithy.kotlin.testing)
    testImplementation(libs.kotlinx.coroutines.core)
}

gradlePlugin {
    plugins {
        create("sdk-bootstrap") {
            id = "sdk-bootstrap"
            implementationClass = "aws.sdk.kotlin.gradle.sdk.Bootstrap"
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showStackTraces = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
