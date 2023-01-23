plugins {
    kotlin("jvm")
    id("org.gradle.kotlin.kotlin-dsl")
    `maven-publish`
    id("com.github.gmazzo.buildconfig")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}

kotlin {
    explicitApi()
    target.compilations.all {
        kotlinOptions.jvmTarget = "11"
    }
}

buildConfig {
    packageName("org.kodein.internal.gradle.settings")
    useKotlinOutput {
        internalVisibility = true
    }
    buildConfigField("String", "kotlinVersion", libs.versions.kotlin.map { "\"$it\"" })
    buildConfigField("String", "thisVersion", "\"$version\"")
}
