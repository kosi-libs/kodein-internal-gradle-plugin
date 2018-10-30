package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kjs.jstests.JsTestsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KodeinMppPlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    private fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin<JsTestsPlugin>()
        }

        extensions.add("kodein", KodeinMPP())

        extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            sourceSets.apply {
                getByName("commonMain") {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                    }
                    languageSettings.progressiveMode = true
                }
                getByName("commonTest") {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-test-common")
                        implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                    }
                }
            }
        }

        printTestLogs()
    }

    override fun apply(target: Project) = target.applyPlugin()

}
