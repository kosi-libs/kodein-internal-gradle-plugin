plugins {
    id("com.gradle.develocity") version "3.19"
}

rootProject.name = "kodein-internal-gradle-plugin"

include(
    "kodein-internal-gradle-settings",
    "kodein-internal-gradle-version-catalog"
)

dependencyResolutionManagement {
    versionCatalogs {
        create("kodeinGlobals") {
            from(files("gradle/globals.versions.toml"))
        }
    }
}

System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")

val isCI = System.getenv("CI") != null

develocity {
    buildScan {
        publishing.onlyIf { isCI }
        uploadInBackground = isCI
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
    }
}
