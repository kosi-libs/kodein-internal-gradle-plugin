package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

object KodeinVersions {

    const val kotlin = "1.2.41"

    const val konan = "0.8-dev-2179"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
