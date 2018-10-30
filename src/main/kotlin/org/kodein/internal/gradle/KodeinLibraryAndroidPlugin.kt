package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.plugin

class KodeinLibraryAndroidPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin<KodeinAndroidPlugin>()
            plugin("org.gradle.maven-publish")
            plugin("digital.wup.android-maven-publish")
            plugin<KodeinUploadPlugin>()
        }

        @Suppress("UnstableApiUsage")
        extensions.configure<PublishingExtension>("publishing") {
            (publications) {
                "Kodein"(MavenPublication::class) {
                    from(components["android"])
                }
            }
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
