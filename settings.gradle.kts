rootProject.name = "kodein-internal-gradle-plugin"

include(
    "kodein-internal-gradle-settings",
    "kodein-internal-gradle-version-catalog"
)

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("kodeinGlobals") {
            from(files("gradle/globals.versions.toml"))
        }
    }
}

System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")
