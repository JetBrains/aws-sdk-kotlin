/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
    val kotlin_repo_url: String? by project
    logger.info("[COMMUNITY_PROJECT_BUILD] kotlin repository url: $kotlin_repo_url")
    kotlin_repo_url?.also { maven(it) }
}

description = "Test utilities for integration and e2e tests"

val smithyKotlinVersion: String by project

dependencies {
    api("aws.smithy.kotlin:http-client-engine-default:$smithyKotlinVersion")
    api("aws.smithy.kotlin:http-client-engine-crt:$smithyKotlinVersion")
}
