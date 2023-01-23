plugins {
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
    kotlin("jvm") version "1.7.10"
    `kotlin-dsl`
    `maven-publish`
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.okhttp3:okhttp:4.9.0")
    }
}

allprojects {
    group = "org.kodein.internal.gradle"
    version = "7.0.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://ajoberstar.github.io/bintray-backup/")
}

dependencies {
    val kotlinVersion = kodeinGlobals.versions.kotlin.get()
    val androidBuildToolsVersion = kodeinGlobals.versions.android.buildTools.get()
    val dokkaVersion = kodeinGlobals.versions.dokka.get()

    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    api(kotlin("gradle-plugin", version = kotlinVersion))
    api(kotlin("serialization", version = kotlinVersion))
    api("com.android.tools.build:gradle:$androidBuildToolsVersion")
    api("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    api("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    api("com.gradle.publish:plugin-publish-plugin:1.1.0")
}

kotlin {
    explicitApi()

    target.compilations.all {
        kotlinOptions.jvmTarget = "11"
    }

    sourceSets.all {
        languageSettings.progressiveMode = true
    }
}

allprojects {
    afterEvaluate {
        if (name != "kodein-internal-gradle-version-catalog") {
            val sourcesJar = task<Jar>("sourcesJar") {
                archiveClassifier.set("sources")
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                from(sourceSets["main"].allSource)
            }

            publishing.publications {
                withType<MavenPublication> {
                    artifact(sourcesJar) {
                        classifier = "sources"
                    }
                }
            }
        }

        publishing.publications {
            withType<MavenPublication> {
                pom {
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    url.set("https://github.com/kosi-libs/kodein-internal-gradle-plugin")
                    issueManagement {
                        name.set("Github")
                        url.set("https://github.com/kosi-libs/kodein-internal-gradle-plugin/issues")
                    }
                    scm {
                        connection.set("https://github.com/kosi-libs/kodein-internal-gradle-plugin.git")
                        url.set("https://github.com/kosi-libs/kodein-internal-gradle-plugin")
                    }
                }
            }
        }

        publishing.repositories {
            val ghUser = System.getenv("GITHUB_USER")
            val ghToken = System.getenv("GITHUB_TOKEN")
            if(ghUser != null && ghToken != null) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/kosi-libs/kodein-internal-gradle-plugin")
                    credentials {
                        username = ghUser
                        password = ghToken
                    }
                }
            } else {
                logger.warn("Missing git credentials. We won't be able to publish any version.")
            }
        }
    }
}
