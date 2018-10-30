import com.jfrog.bintray.gradle.BintrayExtension

group = "org.kodein.internal.gradle"
version = "2.0.0"

plugins {
    `maven-publish`
    `java-library`
    id("org.gradle.kotlin.kotlin-dsl")
    kotlin("jvm")
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}
