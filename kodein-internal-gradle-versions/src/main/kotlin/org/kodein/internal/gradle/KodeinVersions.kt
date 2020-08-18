package org.kodein.internal.gradle

import org.gradle.api.*

object KodeinVersions {

    const val kotlin = "1.4.0"
    const val androidBuildTools = "4.0.1"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
