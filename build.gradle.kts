import okhttp3.*
import okhttp3.Credentials.basic
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.kodein.internal.gradle.*

plugins {
    kotlin("jvm") version "1.3.72"
    `kotlin-dsl`
    `maven-publish`
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.squareup.okhttp3:okhttp:4.9.0")
    }
}

allprojects {
    group = "org.kodein.internal.gradle"
    version = "5.3.0"
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

    api(project(":kodein-internal-gradle-versions"))
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${KodeinVersions.kotlin}")
    api("com.android.tools.build:gradle:${KodeinVersions.androidBuildTools}")
    api("org.jetbrains.dokka:dokka-gradle-plugin:${KodeinVersions.dokka}")
    api("com.squareup.okhttp3:okhttp:4.9.0")
}

kotlin.target.compilations.all {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin.sourceSets.all {
    languageSettings.progressiveMode = true
}

allprojects {
    afterEvaluate {
        val bintrayUsername = (properties["org.kodein.bintray.username"] as String?) ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = (properties["org.kodein.bintray.apiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")
        val bintrayUserOrg = (properties["org.kodein.bintray.userOrg"] as String?) ?: System.getenv("BINTRAY_USER_ORG")
        val hasBintray = bintrayUsername != null && bintrayApiKey != null
        if (!hasBintray) logger.warn("Skipping bintray configuration as bintrayUsername or bintrayApiKey is not defined")

        val snapshotNumber: String? by project
        val gitRef: String? by project
        val eapBranch = gitRef?.split("/")?.last() ?: "dev"
        val bintrayVersion = if (snapshotNumber != null) "${project.version}-$eapBranch-$snapshotNumber" else project.version.toString()

        val bintraySubject = bintrayUserOrg ?: bintrayUsername
        val bintrayRepo = if (snapshotNumber != null) "kodein-dev" else "Kodein-Internal-Gradle"

        if (hasBintray) {
            publishing.repositories {
                maven {
                    name = "bintray"
                    setUrl("https://api.bintray.com/maven/$bintraySubject/$bintrayRepo/${project.name}/;publish=0")
                    credentials {
                        username = bintrayUsername
                        password = bintrayApiKey
                    }
                }
            }

            val postBintrayPublish by tasks.creating {
                doLast {
                    val request = Request.Builder()
                            .url("https://api.bintray.com/content/$bintraySubject/$bintrayRepo/${project.name}/$bintrayVersion/publish")
                            .post("{}".toRequestBody("application/json".toMediaType()))
                            .header("Authorization", basic(bintrayUsername, bintrayApiKey))
                            .build()
                    OkHttpClient().newCall(request).execute()
                }
            }

            val postBintrayDiscard by tasks.creating {
                doLast {
                    val request = Request.Builder()
                            .url("https://api.bintray.com/content/$bintraySubject/$bintrayRepo/${project.name}/$bintrayVersion/publish")
                            .post("{ \"discard\": true }".toRequestBody("application/json".toMediaType()))
                            .header("Authorization", basic(bintrayUsername, bintrayApiKey))
                            .build()
                    OkHttpClient().newCall(request).execute()
                }
            }
        }

        val sourcesJar = task<Jar>("sourcesJar") {
            @Suppress("UnstableApiUsage")
            archiveClassifier.set("sources")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(sourceSets["main"].allSource)
        }

        publishing.publications.withType<MavenPublication> {
            version = bintrayVersion
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
}
