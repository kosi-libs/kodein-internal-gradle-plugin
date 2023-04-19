package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

public class KodeinLibraryMppPlugin : KtPlugin<Project> {

    internal companion object {
        fun Project.applyLibrayMppPlugin() {
            apply {
                plugin("org.gradle.maven-publish")
                plugin<KodeinUploadModulePlugin>()
            }

            val kotlin = extensions.getByName<KotlinMultiplatformExtension>("kotlin")
            kotlin.explicitApi()

            tasks.named("publishToMavenLocal").configure {
                doFirst {
                    val kodein = (kotlin as ExtensionAware).extensions["kodein"] as KodeinMppExtension
                    if (kodein.excludedTargets.isNotEmpty()) {
                        logger.warn("Publishing to maven local with excluded targets: ${kodein.excludedTargets.joinToString()}!")
                    }
                }
            }
        }

    }

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
        }

        applyLibrayMppPlugin()
    }

}
