import org.kodein.internal.gradle.*

plugins {
    `maven-publish`
    `java-library`
    `kotlin-dsl`
    kotlin("jvm") version "1.3.72"
}

repositories {
    jcenter()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")

    mavenLocal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation(kotlin("stdlib-jdk7"))

    api(project(":kodein-internal-gradle-versions"))

    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KodeinVersions.kotlin}")

    api("com.android.tools.build:gradle:${KodeinVersions.androidBuildTools}")

    api("org.jetbrains.dokka:dokka-gradle-plugin:0.9.18")

    api("digital.wup:android-maven-publish:3.6.3")
}

allprojects {
    group = "org.kodein.internal.gradle"
    version = "3.7.0"

    afterEvaluate {
        val sourcesJar = task<Jar>("sourcesJar") {
            @Suppress("UnstableApiUsage")
            archiveClassifier.set("sources")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(sourceSets["main"].allSource)
        }

        val bintrayUsername = (properties["bintrayUsername"] as String?) ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = (properties["bintrayApiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")
        val bintrayUserOrg = (properties["bintrayUserOrg"] as String?) ?: System.getenv("BINTRAY_USER_ORG")

        val snapshotNumber: String? by project
        val gitRef: String? by project

        val eapBranch = gitRef?.split("/")?.last() ?: "dev"
        if (snapshotNumber != null) version = "${project.version}-$eapBranch-$snapshotNumber"

        publishing {
            publications {
                create<MavenPublication>("Kodein") {
                    from(components["java"])
                    artifact(sourcesJar) {
                        classifier = "sources"
                    }
                    pom {
                        licenses {
                            license {
                                name.set("MIT")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        url.set("https://github.com/Kodein-Framework/kodein-internal-gradle-plugin")
                        issueManagement {
                            name.set("Github")
                            url.set("https://github.com/Kodein-Framework/kodein-internal-gradle-plugin/issues")
                        }
                        scm {
                            connection.set("https://github.com/Kodein-Framework/kodein-internal-gradle-plugin.git")
                        }
                    }
                }
            }

            if (bintrayUsername != null && bintrayApiKey != null) {
                repositories {
                    maven {
                        name = "bintray"
                        val isSnaphost = if (snapshotNumber != null) 1 else 0
                        val btSubject = bintrayUserOrg ?: bintrayUsername
                        val btRepo = if (snapshotNumber != null) "kodein-dev" else "Kodein-Internal-Gradle"
                        setUrl("https://api.bintray.com/maven/$btSubject/$btRepo/${project.name}/;publish=$isSnaphost;override=$isSnaphost")
                        credentials {
                            username = bintrayUsername
                            password = bintrayApiKey
                        }
                    }
                }
            }
        }
    }
}
