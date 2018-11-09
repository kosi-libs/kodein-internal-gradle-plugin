import com.jfrog.bintray.gradle.BintrayExtension

group = "org.kodein.internal.gradle"
version = "2.0.0"

plugins {
    `maven-publish`
    `java-library`
    `kotlin-dsl`
    kotlin("jvm") version "1.2.31"
    id("com.jfrog.bintray") version "1.8.0"
}

object KodeinVersions {
    const val kotlinGradle = "1.2.31"
    const val kotlin = "1.3.0"
    const val androidBuild = "3.1.4"
}

repositories {
    jcenter()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    maven(url = "https://dl.bintray.com/salomonbrys/gradle-plugins")
    maven(url = "https://dl.bintray.com/salomonbrys/wup-digital-maven")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")

    mavenLocal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlinGradle}")

    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KodeinVersions.kotlin}")

    api("com.android.tools.build:gradle:${KodeinVersions.androidBuild}")

    api("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")

    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.2-SNAPSHOT")
    api("digital.wup:android-maven-publish:3.5.1-PR21")

    api("com.github.salomonbrys.gradle.kotlin.js:kotlin-js-gradle-utils:1.0.0")
}

allprojects {
    afterEvaluate {
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
            val bintrayUserOrg: String? by project

            bintray {
                user = bintrayUsername
                key = bintrayApiKey

                pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
                    if (bintrayUserOrg != null)
                        userOrg = bintrayUserOrg
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
    }
}
