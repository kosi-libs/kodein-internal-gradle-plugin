package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.withType

class KodeinPlatformJvm : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-jvm")
            plugin<KodeinDokka>()
            plugin<KodeinKotlinPublish>()
            plugin<KodeinPublicationUpload>()
            plugin<KodeinVersionsPlugin>()
        }

        afterEvaluate {
            tasks.withType<Test>().forEach {
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                }
            }
        }

        DependencyHandlerScope(dependencies).apply {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
            "testImplementation"("junit:junit:4.12")
        }

    }

    override fun apply(project: Project) = project.applyPlugin()

}
