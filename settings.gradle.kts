plugins {
    id("com.gradle.enterprise") version "3.18.1"
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

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}
