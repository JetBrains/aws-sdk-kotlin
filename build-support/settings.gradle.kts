/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        val kotlin_repo_url: String? by settings
        kotlin_repo_url?.let { maven(it) }
    }
}

rootProject.name = "build-support"

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
                println("[build-support] Overriding kotlin-version to ${kotlinVersionOverride.get()}")
                version("kotlin-version", kotlinVersionOverride.get())
            }
        }
    }
}
