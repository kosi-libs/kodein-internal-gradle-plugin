rootProject.name = "kodein-internal-gradle-plugin"

include(
        "kodein-internal-gradle-versions",
        "kodein-internal-gradle-settings"
)

System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")
