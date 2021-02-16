import okhttp3.*
import org.kodein.internal.gradle.*

plugins {
    kotlin("jvm") version "1.3.72"
    `kotlin-dsl`
    `maven-publish`
    id("org.ajoberstar.git-publish") version "3.0.0"
    id("org.ajoberstar.grgit") version "4.1.0"
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
    version = "5.6.0"
}

repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://ajoberstar.github.io/bintray-backup/")
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

fun getPublishingVersion(): String {
    val snapshotNumber: String? by project
    val gitRef: String? by project
    val eapBranch = gitRef?.split("/")?.last() ?: "dev"
    return if (snapshotNumber != null) "${project.version}-$eapBranch-$snapshotNumber" else project.version.toString()
}

allprojects {
    afterEvaluate {
        val sourcesJar = task<Jar>("sourcesJar") {
            @Suppress("UnstableApiUsage")
            archiveClassifier.set("sources")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(sourceSets["main"].allSource)
        }

        publishing.publications.withType<MavenPublication> {
            version = getPublishingVersion()
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
                    url.set("https://github.com/Kodein-Framework/kodein-internal-gradle-plugin")
                }
            }
        }
    }
}

task("validateVersionBeforeGitPublish") {
    group = "publishing"
    doLast {
        val publishingVersion = getPublishingVersion()
        val url = "https://github.com/Kodein-Framework/kodein-internal-gradle-plugin/"
        val request = Request.Builder()
            .url("$url/raw/mvn-repo/org/kodein/internal/gradle/kodein-internal-gradle-plugin/$publishingVersion")
            .get()
            .build()
        val res = OkHttpClient().newCall(request).execute()
        if (res.code == 200) error("Version ($publishingVersion) already published on $url.")
    }
}

task("copyMavenLocalArtifacts") {
    group = "publishing"
    doLast {
        val dest = File("$projectDir/build/mvn-repo")
        dest.deleteRecursively()

        val userHome = System.getProperty("user.home")
        val localRepository = "$userHome/.m2/repository/org/kodein/internal/gradle/"
        val dir = File(localRepository)

        val publishingVersion = getPublishingVersion()

        (dir.listFiles() ?: error("$localRepository is empty."))
            .filter { it.isDirectory }
            .forEach { projectDir ->
                File(projectDir, publishingVersion).listFiles()
                    ?.filter { it.isFile }
                    ?.forEach {
                        val destProjectDir = File(dest, "org/kodein/internal/gradle/${projectDir.name}/${version}")
                        destProjectDir.mkdirs()
                        it.copyTo(File(destProjectDir, it.name))
                    }
            }

        dest.listFiles() ?: error("Nothing to publish. Try to call publishToMavenLocal first.")
    }
}

val (gitUser, gitPassword) = (project.findProperty("com.github.http.auth") as? String)?.run {
    val auth = split(":")
    auth[0] to auth[1]
} ?: System.getenv("GIT_USER") as String? to System.getenv("GIT_PASSWORD") as String?
if(gitUser != null && gitPassword != null) {
    System.setProperty("org.ajoberstar.grgit.auth.username", gitUser)
    System.setProperty("org.ajoberstar.grgit.auth.password", gitPassword)
} else {
    logger.warn("Missing git credentials. We won't be able to publish any version.")
}

gitPublish {
    repoUri.set("https://github.com/Kodein-Framework/kodein-internal-gradle-plugin.git")
    branch.set("mvn-repo")
    contents.apply {
        from("$projectDir/build/mvn-repo")
    }
    preserve {
        include("**/**")
    }
    val head = grgit.head()
    commitMessage.set("${head.abbreviatedId}: ${getPublishingVersion()}")
}

task("publishToGithub") {
    group = "publishing"
    dependsOn("validateVersionBeforeGitPublish", "copyMavenLocalArtifacts", "gitPublishPush")
}

tasks["gitPublishCommit"].doFirst {
    if (!grgit.status().isClean) {
        error("Refusing to commit new pages on a non-clean repo. Please commit first.\n${grgit.status()}")
    }
}
