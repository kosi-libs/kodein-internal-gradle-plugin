package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.plugin

@Suppress("UnstableApiUsage")
class KodeinLibraryMppPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
            plugin("org.gradle.maven-publish")
            plugin<KodeinUploadPlugin>()
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
