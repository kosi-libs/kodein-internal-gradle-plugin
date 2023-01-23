package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.register

public class KodeinRootPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinUploadRootPlugin>()
        }

        allprojects {
            tasks.register<DependencyReportTask>("allDependencies")
        }
    }

}
