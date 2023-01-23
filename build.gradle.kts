//import okhttp3.OkHttpClient
//import okhttp3.Request

plugins {
    kotlin("jvm") version "1.6.21"
    `kotlin-dsl`
    `maven-publish`
    id("org.ajoberstar.git-publish") version "3.0.0"
    id("org.ajoberstar.grgit") version "4.1.0"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
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
    val kotlinVersion = libs.versions.kotlin.get()
    val androidBuildToolsVersion = libs.versions.android.buildTools.get()
    val dokkaVersion = libs.versions.dokka.get()

    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    api(kotlin("gradle-plugin", version = kotlinVersion))
    api(kotlin("serialization", version = kotlinVersion))
    api("com.android.tools.build:gradle:$androidBuildToolsVersion")
    api("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    api("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    api("com.gradle.publish:plugin-publish-plugin:0.18.0")
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

buildConfig {
    packageName("org.kodein.internal.gradle")
    useKotlinOutput {
        internalVisibility = true
    }
    buildConfigField("String", "kotlinVersion", libs.versions.kotlin.map { "\"$it\"" })
    buildConfigField("String", "androidBuildToolsVersion", libs.versions.android.buildTools.map { "\"$it\"" })
    buildConfigField("String", "androidNdkVersion", libs.versions.android.ndk.map { "\"$it\"" })
    buildConfigField("String", "dokkaVersion", libs.versions.dokka.map { "\"$it\"" })
}

//fun getPublishingVersion(): String {
//    val snapshotNumber: String? by project
//    val gitRef: String? by project
//    val eapBranch = gitRef?.split("/")?.last() ?: "dev"
//    return if (snapshotNumber != null) "${project.version}-$eapBranch-$snapshotNumber" else project.version.toString()
//}

allprojects {
    afterEvaluate {
        val sourcesJar = task<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(sourceSets["main"].allSource)
        }

        publishing.publications.withType<MavenPublication> {
//            version = getPublishingVersion()
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

//task("validateVersionBeforeGitPublish") {
//    group = "publishing"
//    doLast {
//        val publishingVersion = getPublishingVersion()
//
//        // Allow snapshots to be overridden
//        if(!publishingVersion.matches("""^(\d*)\.(\d*)\.(\d*)$""".toRegex())) return@doLast
//
//        // Verify that the SemVer version does not exist on the mvn-repo branch
//        //  https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo/org/kodein/internal/gradle/kodein-internal-gradle-plugin/5.5.0/kodein-internal-gradle-plugin-5.5.0.pom
//        val url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin"
//        val request = Request.Builder()
//            .url("$url/mvn-repo/org/kodein/internal/gradle/kodein-internal-gradle-plugin/$publishingVersion/kodein-internal-gradle-plugin-$publishingVersion.pom")
//            .get()
//            .build()
//        val res = OkHttpClient().newCall(request).execute()
//        if (res.code == 200) error("Version ($publishingVersion) already published on $url.")
//    }
//}
//
//tasks.create<Sync>("copyMavenLocalArtifacts") {
//    group = "publishing"
//    dependsOn("validateVersionBeforeGitPublish")
//    val publishingVersion = getPublishingVersion()
//    val userHome = System.getProperty("user.home")
//    val groupDir = project.group.toString().replace('.', '/')
//    val localRepository = "$userHome/.m2/repository/$groupDir/"
//
//    from(localRepository) {
//        include("*/$publishingVersion/**")
//    }
//
//    into("$buildDir/mvn-repo/$groupDir/")
//}
//
//val (gitUser, gitPassword) = (project.findProperty("com.github.http.auth") as? String)?.run {
//    val auth = split(":")
//    auth[0] to auth[1]
//} ?: (System.getenv("GIT_USER") to System.getenv("GIT_PASSWORD")) as Pair<String?, String?>
//if(gitUser != null && gitPassword != null) {
//    System.setProperty("org.ajoberstar.grgit.auth.username", gitUser)
//    System.setProperty("org.ajoberstar.grgit.auth.password", gitPassword)
//} else {
//    logger.warn("Missing git credentials. We won't be able to publish any version.")
//}
//
//gitPublish {
//    repoUri.set("https://github.com/kosi-libs/kodein-internal-gradle-plugin.git")
//    branch.set("mvn-repo")
//    contents {
//        from("$buildDir/mvn-repo")
//    }
//    preserve {
//        include("**")
//    }
//    val head = grgit.head()
//    commitMessage.set("${head.abbreviatedId}: ${getPublishingVersion()} : ${head.fullMessage}")
//}
//
//tasks["gitPublishCopy"].dependsOn("copyMavenLocalArtifacts")
//
//tasks["gitPublishCommit"].doFirst {
//    if (!grgit.status().isClean) {
//        error("Refusing to commit new pages on a non-clean repo. Please commit first.\n${grgit.status()}")
//    }
//}
