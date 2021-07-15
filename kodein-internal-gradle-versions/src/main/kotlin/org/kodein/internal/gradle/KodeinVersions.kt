package org.kodein.internal.gradle

import org.gradle.api.*

object KodeinVersions {

    const val kotlin = "1.5.10"
    const val dokka = "1.5.0-dev-99"
    const val androidBuildTools = "4.1.3"
    const val androidNdk = "21.3.6528147"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
