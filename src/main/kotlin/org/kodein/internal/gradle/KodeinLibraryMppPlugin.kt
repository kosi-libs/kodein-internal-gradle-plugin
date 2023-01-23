package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("UnstableApiUsage")
class KodeinLibraryMppPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
            plugin("org.gradle.maven-publish")
            plugin<KodeinUploadModulePlugin>()
        }

        extensions.getByName<KotlinMultiplatformExtension>("kotlin").explicitApi()

        tasks.named("publishToMavenLocal").configure {
            doFirst {
                val k = project.extensions["kodein"] as KodeinMppExtension
                if (k.excludedTargets.isNotEmpty()) {
                    logger.warn("Publishing to maven local with excluded targets: ${k.excludedTargets.joinToString { it.name }}!")
                }
            }
        }
    }

}
