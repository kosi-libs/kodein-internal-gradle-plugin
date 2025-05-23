plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.buildconfig)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    api(libs.gradle.develocityPlugin)
}

kotlin {
    explicitApi()
    jvmToolchain(17)
}

buildConfig {
    packageName("org.kodein.internal.gradle.settings")
    useKotlinOutput {
        internalVisibility = true
    }
    buildConfigField("String", "kotlinVersion", kodeinGlobals.versions.kotlin.map { "\"$it\"" })
    buildConfigField("String", "thisVersion", "\"$version\"")
}
