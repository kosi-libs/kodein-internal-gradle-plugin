package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class KodeinJvmPlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    private fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-jvm")
            plugin("org.gradle.maven-publish")
        }

        DependencyHandlerScope(dependencies).apply {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
        }

        afterEvaluate {
            extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets.forEach { it.languageSettings.progressiveMode = true }
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
