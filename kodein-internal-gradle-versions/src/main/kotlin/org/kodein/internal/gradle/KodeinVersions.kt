package org.kodein.internal.gradle

import org.gradle.api.*

@Suppress("unused")
object KodeinVersions {

    const val kotlin = "1.7.20"
    const val dokka = "1.7.10"
    const val androidBuildTools = "7.0.0"
    const val androidNdk = "21.3.6528147"

}

class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project) = project.applyPlugin()

}
