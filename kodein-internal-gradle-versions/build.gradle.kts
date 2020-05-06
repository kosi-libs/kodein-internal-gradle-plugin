plugins {
    `maven-publish`
    `java-library`
    id("org.gradle.kotlin.kotlin-dsl")
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}
