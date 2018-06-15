import com.jfrog.bintray.gradle.BintrayExtension

group = "org.kodein.internal.gradle"
version = "1.1.1"

plugins {
    `maven-publish`
    `kotlin-dsl`
    `java-library`
    kotlin("jvm") version "1.2.41"
    id("com.jfrog.bintray") version "1.8.0"
}

object KodeinVersions {

    const val kotlin = "1.2.41"

    const val konan = "0.8-dev-2179"

}

repositories {
    jcenter()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    maven(url = "https://dl.bintray.com/salomonbrys/KMP-Gradle-Utils")
    maven(url = "https://dl.bintray.com/salomonbrys/wup-digital-maven")

    mavenLocal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KodeinVersions.kotlin}")
    api("org.jetbrains.kotlin:kotlin-native-gradle-plugin:${KodeinVersions.konan}")

    api("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.2-SNAPSHOT")
    api("com.android.tools.build:gradle:3.1.2")
    api("digital.wup:android-maven-publish:3.5.1-PR21")

    val kmpVer = "1.0.3"
    api("com.github.salomonbrys.gradle:all-sources-jar:$kmpVer")
    api("com.github.salomonbrys.gradle:js-tests:$kmpVer")
    api("com.github.salomonbrys.gradle:assemble-web:$kmpVer")
    api("com.github.salomonbrys.gradle:konan-tests:$kmpVer")
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

if (hasProperty("bintrayUsername") && hasProperty("bintrayApiKey")) {
    val bintrayUsername: String by project
    val bintrayApiKey: String by project

    bintray {
        user = bintrayUsername
        key = bintrayApiKey

        pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
            userOrg = "kodein-framework"
            repo = "Kodein-Internal-Gradle"
            name = project.name
            setLicenses("MIT")
            websiteUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin"
            issueTrackerUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin/issues"
            vcsUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin.git"

            setPublications("Kodein")
        })

    }
}
