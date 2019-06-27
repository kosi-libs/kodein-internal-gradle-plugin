import org.kodein.internal.gradle.*

plugins {
    `maven-publish`
    `java-library`
    `kotlin-dsl`
    kotlin("jvm") version "1.3.21"
    id("com.jfrog.bintray") version "1.8.4"
}

val gradleKotlin = "1.3.21"

repositories {
    jcenter()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/salomonbrys/gradle-plugins")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")

    mavenLocal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$gradleKotlin")

    api(project(":kodein-internal-gradle-versions"))

    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KodeinVersions.kotlin}")

    api("com.android.tools.build:gradle:${KodeinVersions.androidBuildTools}")

    api("org.jetbrains.dokka:dokka-gradle-plugin:0.9.18")

    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4-jetbrains-5")
    api("digital.wup:android-maven-publish:3.6.2")

    api("com.github.salomonbrys.gradle.kotlin.js:kotlin-js-gradle-utils:1.2.0")
}

allprojects {
    group = "org.kodein.internal.gradle"
    version = "2.5.3"

    afterEvaluate {
        val sourcesJar = task<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
            from(sourceSets["main"].allSource)
        }

        publishing {
            publications {
                create<MavenPublication>("Kodein") {
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

                pkg.apply {
                    if (bintrayUserOrg != null)
                        userOrg = bintrayUserOrg
                    repo = "Kodein-Internal-Gradle"
                    name = project.name
                    setLicenses("MIT")
                    websiteUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin"
                    issueTrackerUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin/issues"
                    vcsUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin.git"

                    setPublications("Kodein")
                }

            }
        }
    }
}
