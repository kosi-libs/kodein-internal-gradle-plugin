package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import java.lang.IllegalStateException

class KodeinUploadRootPlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    private fun Project.applyPlugin() {
        val ext = KodeinRootUploadExtension()
        project.extensions.add(KodeinUploadExtension::class.java, "kodeinUpload", ext)

        subprojects {
            apply(plugin = "org.kodein.upload")
        }
    }

    override fun apply(project: Project) = project.applyPlugin()
}
