package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KodeinPublicationsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        val ext = KodeinPublicationsExtension()
        project.extensions.add("kodeinPublications", ext)
    }

    override fun apply(project: Project) = project.applyPlugin()
}
