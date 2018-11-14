package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KodeinUploadRootPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        val ext = KodeinRootUploadExtension()
        project.extensions.add(KodeinUploadExtension::class.java, "kodeinUpload", ext)

        subprojects {
            apply(plugin = "org.kodein.upload")
        }
    }

}
