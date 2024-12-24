/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import aws.sdk.kotlin.gradle.dsl.configureLinting
import aws.sdk.kotlin.gradle.dsl.configureMinorVersionStrategyRules
import aws.sdk.kotlin.gradle.publishing.SonatypeCentralPortalPublishTask
import aws.sdk.kotlin.gradle.publishing.SonatypeCentralPortalWaitForPublicationTask
import aws.sdk.kotlin.gradle.publishing.configureJarReduction
import aws.sdk.kotlin.gradle.util.typedProp
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URL

buildscript {
    // NOTE: buildscript classpath for the root project is the parent classloader for the subprojects, we
    // only need to add e.g. atomic-fu and build-plugins here for imports and plugins to be available in subprojects.
    dependencies {
        classpath(libs.kotlinx.atomicfu.plugin)
        // Add our custom gradle build logic to buildscript classpath
        classpath(libs.aws.kotlin.repo.tools.build.support)

        // WA for https://youtrack.jetbrains.com/issue/KT-73357/
        // remove after updated dependencies will not bring an old version.
        classpath("org.jetbrains.kotlin:kotlin-compiler-embeddable:${libs.versions.kotlin.version.get()}")
    }

    configurations.classpath {
        resolutionStrategy {
            /*
            Version bumping the SDK to 1.5.x in repo tools broke our buildscript classpath:
            java.lang.NoSuchMethodError: 'void kotlinx.coroutines.CancellableContinuation.resume(java.lang.Object, kotlin.jvm.functions.Function3)

            FIXME: Figure out what broke our buildscript classpath, this is a temporary fix
             */
            force("com.squareup.okhttp3:okhttp-coroutines:5.5.0")
        }
    }
}

plugins {
    `dokka-convention`
    // ensure the correct version of KGP ends up on our buildscript classpath
    id(libs.plugins.kotlin.multiplatform.get().pluginId) apply false
    id(libs.plugins.kotlin.jvm.get().pluginId) apply false
}

val testJavaVersion = typedProp<String>("test.java.version")?.let {
    JavaLanguageVersion.of(it)
}?.also {
    println("configuring tests to run with jdk $it")
}

allprojects {
    if (rootProject.typedProp<Boolean>("kotlinWarningsAsErrors") == true) {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            compilerOptions.allWarningsAsErrors = true
        }
    }

    tasks.withType<Test>().configureEach {
        if (testJavaVersion != null) {
            val toolchains = project.extensions.getByType<JavaToolchainService>()
            javaLauncher.set(
                toolchains.launcherFor {
                    languageVersion.set(testJavaVersion)
                },
            )
        }

        if (testJavaVersion == null || testJavaVersion.asInt() >= 9) {
            // Required to enable reflective access in testing in JDK 9+.
            // See smithy-kotlin/runtime/testing/jvm/src/aws/smithy/kotlin/runtime/testing/SystemOverrides.kt for more
            // info.
            jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
        }
    }

    // Enables running `./gradlew allDeps` to get a comprehensive list of dependencies for every subproject
    tasks.register<DependencyReportTask>("allDeps") { }
}

// Configure root module's documentation
dokka {
    moduleName.set("AWS SDK for Kotlin")

    dokkaPublications.html {
        includes.from(
            rootProject.file("docs/dokka-presets/README.md"),
        )
    }
}

// Aggregate subprojects' documentation
dependencies {
    dokka(project(":aws-runtime"))
    dokka(project(":services"))
    dokka(project(":hll"))
}

// Code Style
val lintPaths = listOf(
    "**/*.{kt,kts}",
    "!**/generated-src/**",
    "!**/generated/ksp/**",
    "!**/kspCaches/**",
    "!**/smithyprojections/**",
    "!**/build/**",
)

configureLinting(lintPaths)
configureMinorVersionStrategyRules(lintPaths)

tasks.register<SonatypeCentralPortalPublishTask>("publishToCentralPortal") { }
tasks.register<SonatypeCentralPortalWaitForPublicationTask>("waitForCentralPortalPublication") { }

configureJarReduction("aws/sdk/kotlin")
fun HasConfigurableKotlinCompilerOptions<*>.configureCompilerOptions(name: String) {
    compilerOptions {
        val kotlin_api_version: String? by project
        val kotlin_language_version: String? by project

        kotlin_language_version?.let {
            println("[$name] Custom LanguageVersion: $it")
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.fromVersion(it))
        }

        kotlin_api_version?.let {
            println("[$name] Custom ApiVersion: $it")
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.fromVersion(it))
        }

        freeCompilerArgs.add("-Xpartial-linkage-loglevel=ERROR")
    }
}

subprojects{
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmExtension> {
            configureCompilerOptions(project.name)
        }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        if (project.name != "dynamodb-mapper") {
            configure<KotlinMultiplatformExtension> {
                configureCompilerOptions(project.name)
            }
        }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        configure<KotlinAndroidExtension> {
            configureCompilerOptions(project.name)
        }
    }
}
