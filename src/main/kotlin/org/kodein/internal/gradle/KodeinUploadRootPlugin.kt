package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import java.lang.IllegalStateException

class KodeinUploadRootPlugin : Plugin<Project> {
    private fun Project.applyPlugin() {
        val ext = KodeinUploadExtension()
        project.extensions.add("kodeinUpload", ext)

        subprojects {
            apply(plugin = "org.kodein.upload")
        }

        afterEvaluate {
            if (ext != KodeinUploadExtension())
                throw IllegalStateException("You must not configure this project's kodeinUpload. It is only there to trick gradle into allowing subprojects { kodeinUpload {} }")
        }
    }

    override fun apply(project: Project) = project.applyPlugin()
}
