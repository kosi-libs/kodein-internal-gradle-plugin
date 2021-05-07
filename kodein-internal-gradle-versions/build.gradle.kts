plugins {
    kotlin("jvm")
    id("org.gradle.kotlin.kotlin-dsl")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}

kotlin.target.compilations.all {
    kotlinOptions.jvmTarget = "1.8"
}
