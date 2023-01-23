package org.kodein.internal.gradle.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.plugins.DslObject
import org.gradle.kotlin.dsl.maven
import java.io.File
import java.util.*

@Suppress("UnstableApiUsage")
public class KodeinSettingsPlugin : Plugin<Settings> {

    private lateinit var settings: Settings

    private val localProperties: Properties by lazy {
        File("${settings.rootDir}/kodein.local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { Properties().apply { load(it) } }
            ?: Properties()

    }

    public fun findLocalProperty(key: String): String? =
        System.getenv("KODEIN_LOCAL_${key.toUpperCase()}")
            ?: localProperties.getProperty(key)
            ?: DslObject(settings).asDynamicObject.tryGetProperty("org.kodein.local.$key")
                .takeIf { it.isFound } ?.value as String?

    private fun Settings.applyPlugin() {
        dependencyResolutionManagement {
            repositories {
                repositories {
                    mavenLocal()
                    mavenCentral()
                    google()
                    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
                    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
                    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
                }
            }

            @Suppress("UnstableApiUsage")
            versionCatalogs {
                create("kodeinGlobals") {
                    from("org.kodein.internal.gradle:kodein-internal-gradle-version-catalog:${BuildConfig.thisVersion}")
                }
            }
        }

        pluginManagement {
            repositories {
                mavenLocal()
                mavenCentral()
                google()
                gradlePluginPortal()
                maven(url = "https://plugins.gradle.org/m2/")
                maven(url = "https://maven.pkg.github.com/kosi-libs/kodein-internal-gradle-plugin")
                maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
                maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
            }

            resolutionStrategy {
                eachPlugin {
                    val id = requested.id.id
                    when {
                        id.startsWith("org.kodein.") -> useModule("org.kodein.internal.gradle:kodein-internal-gradle-plugin:${BuildConfig.thisVersion}")
                        id == "kotlinx-serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${BuildConfig.kotlinVersion}")
                    }
                }
            }
        }

        enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
    }

    override fun apply(settings: Settings) {
        this.settings = settings
        settings.applyPlugin()
    }

    internal companion object {
        fun get(settings: Settings): KodeinSettingsPlugin = settings.plugins.getPlugin(KodeinSettingsPlugin::class.java)
    }
}
