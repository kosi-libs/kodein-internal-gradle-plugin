package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.jetbrains.dokka.gradle.DokkaTask

class KodeinDokka : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply { plugin("org.jetbrains.dokka") }

        val dokka = tasks["dokka"] as DokkaTask
        dokka.apply {
            outputFormat = "html"
            outputDirectory = "$buildDir/javadoc"

            reportUndocumented = true
        }

        task<Jar>("dokkaJar") {
            classifier = "javadoc"
            from(dokka)
        }
    }

    override fun apply(project: Project) = project.applyPlugin()
}
