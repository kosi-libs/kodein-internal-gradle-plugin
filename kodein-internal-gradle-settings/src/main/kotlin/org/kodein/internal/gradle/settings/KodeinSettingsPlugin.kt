package org.kodein.internal.gradle.settings

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.plugins.DslObject
import org.gradle.kotlin.dsl.extra
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
        System.getenv("KODEIN_LOCAL_${key.uppercase(Locale.getDefault())}")
            ?: localProperties.getProperty(key)
            ?: DslObject(settings).asDynamicObject.tryGetProperty("org.kodein.local.$key")
                .takeIf { it.isFound } ?.value as String?

    private fun Settings.applyPlugin() {
        dependencyResolutionManagement {
            repositories {
                mavenLocal()
                mavenCentral()
                google()
                maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
                maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
                maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
                maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
                maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
            }

            versionCatalogs {
                create("kodeinGlobals") {
                    from("org.kodein.internal.gradle:kodein-internal-gradle-version-catalog:${BuildConfig.thisVersion}")
                }
            }
        }

        pluginManagement {
            repositories {
                mavenLocal()
                gradlePluginPortal()
                mavenCentral()
                google()
                maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
                maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
                maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
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

        this.gradle.allprojects {
            extra.set("kotlin.native.ignoreDisabledTargets", "true")
            extra.set("kotlin.mpp.stability.nowarn", "true")
            extra.set("kotlin.mpp.androidSourceSetLayoutVersion", "2")
        }

        plugins.apply("com.gradle.enterprise")

        extensions.configure<GradleEnterpriseExtension>(GradleEnterpriseExtension.NAME) {
            if (System.getenv("CI") != null) {
                buildScan {
                    publishAlways()
                    termsOfServiceUrl = "https://gradle.com/terms-of-service"
                    termsOfServiceAgree = "yes"
                }
            }
        }
    }

    override fun apply(settings: Settings) {
        this.settings = settings
        settings.applyPlugin()
    }

    internal companion object {
        fun get(settings: Settings): KodeinSettingsPlugin = settings.plugins.getPlugin(KodeinSettingsPlugin::class.java)
    }
}
