package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

object KodeinVersions {

    const val kotlin = "1.2.70"

    const val konan = "0.9.2"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
