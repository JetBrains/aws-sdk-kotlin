plugins {
    kotlin("jvm") version "1.8.10"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "aws.sdk.kotlin.test"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

val awsSdkKotlinVersion: String by project
val coroutinesVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation("aws.sdk.kotlin:aws-config:$awsSdkKotlinVersion")
    implementation("aws.sdk.kotlin:sts:$awsSdkKotlinVersion")

    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
}

application {
    mainClass.set("aws.sdk.kotlin.test.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "aws.sdk.kotlin.test.MainKt"
    }
}
