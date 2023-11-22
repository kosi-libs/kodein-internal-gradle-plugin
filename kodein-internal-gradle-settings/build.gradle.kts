plugins {
    kotlin("jvm")
    id("org.gradle.kotlin.kotlin-dsl")
    `maven-publish`
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    api(libs.gradle.enterprisePlugin)
}

kotlin {
    explicitApi()
    target.compilations.all {
        kotlinOptions.jvmTarget = "17"
    }
}

buildConfig {
    packageName("org.kodein.internal.gradle.settings")
    useKotlinOutput {
        internalVisibility = true
    }
    buildConfigField("String", "kotlinVersion", kodeinGlobals.versions.kotlin.map { "\"$it\"" })
    buildConfigField("String", "thisVersion", "\"$version\"")
}
