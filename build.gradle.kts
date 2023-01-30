plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    `maven-publish`
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
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kodeinGlobals.kotlin.reflect)

    api(kodeinGlobals.kotlin.gradlePlugin)
    api(kodeinGlobals.kotlin.serializationPlugin)
    api(kodeinGlobals.android.gradlePlugin)
    api(kodeinGlobals.dokka.gradlePlugin)
    api(kodeinGlobals.dokka.core)
    api(libs.gradle.publishPlugin)
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
