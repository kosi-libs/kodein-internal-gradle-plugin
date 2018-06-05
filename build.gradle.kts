group = "org.kodein.internal.gradle"
version = "1.0.0"

plugins {
    `maven-publish`
    `kotlin-dsl`
    `java-library`
    kotlin("jvm") version "1.2.41"
}

object KodeinVersions {

    const val kotlin = "1.2.41"

//    const val konan = "0.7"
    const val konan = "0.8-dev-2179"

}

repositories {
    jcenter()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    maven(url = "https://dl.bintray.com/salomonbrys/KMP-Gradle-Utils")

    mavenLocal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KodeinVersions.kotlin}")
    api("org.jetbrains.kotlin:kotlin-native-gradle-plugin:${KodeinVersions.konan}")

    api("org.jetbrains.dokka:dokka-gradle-plugin:0.9.16")
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.0")
//    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.1-SNAPSHOT")
    api("com.android.tools.build:gradle:3.1.2")
    api("digital.wup:android-maven-publish:3.3.0")

    val kmpVer = "1.0.0"
    api("com.github.salomonbrys.gradle:all-sources-jar:$kmpVer")
    api("com.github.salomonbrys.gradle:js-tests:$kmpVer")
    api("com.github.salomonbrys.gradle:assemble-web:$kmpVer")
}

val sourcesJar = task<Jar>("sourcesJar") {
    classifier = "sources"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(java.sourceSets["main"].allSource)
}

publishing {
    (publications) {
        "Kodein"(MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar) {
                classifier = "sources"
            }
        }
    }
}
