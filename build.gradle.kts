import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.target

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.git.gradle)
    alias(libs.plugins.git.publish)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.okhttp)
    }
}

allprojects {
    group = "org.kodein.internal.gradle"
    version = "8.9.0"
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://ajoberstar.github.io/bintray-backup/")
    mavenLocal()
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
    api(libs.gradle.enterprisePlugin)
}

kotlin {
    explicitApi()

    target.compilations.all {
        kotlinOptions.jvmTarget = "17"
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

            publishing.publications.withType<MavenPublication> {
                artifact(sourcesJar) {
                    classifier = "sources"
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
    }
}

tasks.register("validateVersion") {
    group = "publishing"
    doLast {
        val publishingVersion = project.version.toString()

        // Allow snapshots to be overridden
        if(!publishingVersion.matches("""^(\d*)\.(\d*)\.(\d*)$""".toRegex())) return@doLast

        // Verify that the SemVer version does not exist on the mvn-repo branch
        val url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin"
        val request = Request.Builder()
            .url("$url/mvn-repo/org/kodein/internal/gradle/kodein-internal-gradle-plugin/$publishingVersion/kodein-internal-gradle-plugin-$publishingVersion.pom")
            .get()
            .build()
        val res = OkHttpClient().newCall(request).execute()
        if (res.code == 200) error("Version ($publishingVersion) already published on $url.")
    }
}

val buildDir by layout.buildDirectory
tasks.register<Sync>("copyMavenLocalArtifacts") {
    group = "publishing"
    dependsOn("validateVersion")
    val publishingVersion = project.version.toString()
    val userHome = System.getProperty("user.home")
    val groupDir = project.group.toString().replace('.', '/')
    val localRepository = "$userHome/.m2/repository/$groupDir/"

    from(localRepository) {
        include("*/$publishingVersion/**")
    }

    into("${buildDir}/mvn-repo/$groupDir/")
}

val (gitUser, gitPassword) = (project.findProperty("com.github.http.auth") as? String)?.run {
    val auth = split(":")
    auth[0] to auth[1]
} ?: (System.getenv("GIT_USER") to System.getenv("GIT_PASSWORD")) as Pair<String?, String?>
if(gitUser != null && gitPassword != null) {
    System.setProperty("org.ajoberstar.grgit.auth.username", gitUser)
    System.setProperty("org.ajoberstar.grgit.auth.password", gitPassword)
    Unit
} else {
    logger.warn("Missing git credentials. We won't be able to publish any version.")
}

gitPublish {
    repoUri.set("https://github.com/kosi-libs/kodein-internal-gradle-plugin.git")
    branch.set("mvn-repo")
    contents {
        from("${buildDir}/mvn-repo")
    }
    preserve {
        include("**")
    }
    val head = grgit.head()
    commitMessage.set("${head.abbreviatedId}: ${project.version} : ${head.fullMessage}")
}

tasks.named("gitPublishCopy").configure { dependsOn("copyMavenLocalArtifacts") }

tasks.named("gitPublishCommit").configure {
    doFirst {
        if (!grgit.status().isClean) {
            error("Refusing to commit new pages on a non-clean repo. Please commit first.\n${grgit.status()}")
        }
    }
}
