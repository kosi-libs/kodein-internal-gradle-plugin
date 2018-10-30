package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KodeinJvmPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-jvm")
            plugin("org.gradle.maven-publish")
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
