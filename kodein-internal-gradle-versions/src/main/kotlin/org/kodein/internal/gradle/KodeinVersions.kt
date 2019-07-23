package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

object KodeinVersions {

    const val kotlin = "1.3.41"
    const val androidBuildTools = "3.5.0-rc01"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
