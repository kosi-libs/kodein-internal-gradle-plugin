package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories

public class KodeinRootPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinUploadRootPlugin>()
        }

        allprojects {
            tasks.register<DependencyReportTask>("allDependencies")
        }

        // https://youtrack.jetbrains.com/issue/KT-48410
        // Kaverit: Could not find org.jetbrains.kotlin:kotlin-klib-commonizer-embeddable:1.9.20
        repositories {
            mavenCentral()
        }
    }
}
