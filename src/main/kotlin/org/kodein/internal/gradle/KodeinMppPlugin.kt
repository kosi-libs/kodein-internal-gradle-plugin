package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kotlin.js.jstests.node.KotlinMppJsTestsNodePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KodeinMppPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin<KotlinMppJsTestsNodePlugin>()
        }

        val nativeCommonHost: String? by project

        extensions.add("kodein", KodeinMPP(nativeCommonHost == "true"))

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

            afterEvaluate {
                sourceSets.forEach {
                    it.languageSettings.progressiveMode = true
                }
            }
        }

        printTestLogs()
    }

}
