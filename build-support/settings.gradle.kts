/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
rootProject.name = "build-support"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        val kotlin_repo_url: String? by settings
        kotlin_repo_url?.let { maven(it) }
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        val kotlin_repo_url: String? by settings
        kotlin_repo_url?.let { maven(it) }
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))

            val kotlinVersionOverride = providers.gradleProperty("kotlin_version")
            if (kotlinVersionOverride.isPresent) {
                println("[aws-sdk-kotlin's build-support] Overriding kotlin-version to ${kotlinVersionOverride.get()}")
                version("kotlin-version", kotlinVersionOverride.get())
            }
        }
    }
}