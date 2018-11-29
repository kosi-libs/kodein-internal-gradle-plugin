package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.lang.IllegalStateException

@Suppress("UnstableApiUsage")
class KodeinLibraryMppPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
            plugin("org.gradle.maven-publish")
            plugin<KodeinUploadPlugin>()
        }

        extensions.add("kodeinLib", KodeinLibraryMppExtension(this))
    }

}
