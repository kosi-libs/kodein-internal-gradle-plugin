package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.plugin

@Suppress("UnstableApiUsage")
class KodeinLibraryMppPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
            plugin("org.gradle.maven-publish")
            plugin<KodeinUploadPlugin>()
        }
    }

}
