package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.konanartifacts.KonanTestsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.plugin

class KodeinPlatformNative : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("konan")
            plugin("maven-publish")
            plugin<KodeinPublicationUpload>()
            plugin<KodeinVersionsPlugin>()
            plugin<KonanTestsPlugin>()
        }

        extensions.add("kodeinNative", KodeinNativeExtension())

        extensions.configure<KodeinPublicationExtension>("kodeinPublication") {
            publications = { (project.extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension).publications.toTypedArray() }
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
