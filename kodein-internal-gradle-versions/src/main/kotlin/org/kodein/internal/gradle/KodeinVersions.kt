package org.kodein.internal.gradle

import org.gradle.api.*

object KodeinVersions {

    const val kotlin = "1.3.60"
    const val androidBuildTools = "3.5.0-rc01"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
