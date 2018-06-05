package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.plugin

class KodeinPlatformCommon : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-common")
            plugin<KodeinKotlinPublish>()
            plugin<KodeinPublicationUpload>()
            plugin<KodeinVersionsPlugin>()
        }

        DependencyHandlerScope(dependencies).apply {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-common:${KodeinVersions.kotlin}")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test-common:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-annotations-common:${KodeinVersions.kotlin}")
        }
    }

    override fun apply(project: Project) = project.applyPlugin()
}
