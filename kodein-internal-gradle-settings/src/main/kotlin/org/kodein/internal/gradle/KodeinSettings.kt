package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven

@Suppress("UnstableApiUsage")
class KodeinSettingsPlugin : Plugin<Settings> {

    private fun Settings.applyPlugin() {

        val version = this@applyPlugin.buildscript.configurations["classpath"].dependencies.first { it.group == "org.kodein.internal.gradle" && it.name == "kodein-internal-gradle-settings" } .version

        enableFeaturePreview("GRADLE_METADATA")

        pluginManagement {
            repositories {
                mavenLocal()
                jcenter()
                google()
                maven(url = "https://plugins.gradle.org/m2/")
                maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
                maven(url = "https://dl.bintray.com/salomonbrys/KMP-Gradle-Utils")
                maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
                maven(url = "https://dl.bintray.com/salomonbrys/wup-digital-maven")
                maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
            }

            resolutionStrategy {
                eachPlugin {
                    if (requested.id.id.startsWith("org.kodein.")) {
                        useModule("org.kodein.internal.gradle:kodein-internal-gradle-plugin:$version")
                    }
                }
            }
        }

    }

    override fun apply(settings: Settings) = settings.applyPlugin()
}
