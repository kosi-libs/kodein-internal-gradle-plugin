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
}

allprojects {
    group = "org.kodein.internal.gradle"
    version = "2.9.8"

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

        val bintrayUsername = (properties["bintrayUsername"] as String?) ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = (properties["bintrayApiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")
        val bintrayUserOrg = (properties["bintrayUserOrg"] as String?) ?: System.getenv("BINTRAY_USER_ORG")
        val bintrayDryRun: String? by project
        val snapshotNumber: String? by project

        if (bintrayUsername != null && bintrayApiKey != null) {
            bintray {
                user = bintrayUsername
                key = bintrayApiKey
                dryRun = bintrayDryRun == "true"

                pkg.apply {
                    if (bintrayUserOrg != null)
                        userOrg = bintrayUserOrg
                    repo = "Kodein-Internal-Gradle"
                    name = project.name
                    setLicenses("MIT")
                    websiteUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin"
                    issueTrackerUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin/issues"
                    vcsUrl = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin.git"

                    if (snapshotNumber != null){
                        repo = "kodein-dev"
                        project.version = "${project.version}-dev-$snapshotNumber"
                        publish = true
                    }

                    setPublications("Kodein")
                }

            }
        }
    }
}
