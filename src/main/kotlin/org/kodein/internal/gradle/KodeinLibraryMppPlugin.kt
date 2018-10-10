package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kjs.jstests.JsTestsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.withType

@Suppress("UnstableApiUsage")
class KodeinLibraryMppPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin("org.gradle.maven-publish")
            plugin<KodeinVersionsPlugin>()
            plugin<KodeinNativePlugin>()
            plugin<JsTestsPlugin>()
        }

        afterEvaluate {
            tasks.withType<Test>().forEach {
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                }
            }
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
