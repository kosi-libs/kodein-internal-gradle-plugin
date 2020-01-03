package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.plugin

@Suppress("UnstableApiUsage")
class KodeinLibraryMppPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
            plugin("org.gradle.maven-publish")
            plugin<KodeinUploadPlugin>()

            afterEvaluate {
                tasks["publishToMavenLocal"].doFirst {
                    val k = project.extensions["kodein"] as KodeinMPPExtension
                    if (k.excludedTargets.isNotEmpty()) {
                        logger.warn("Publishing to maven local with excluded targets: ${k.excludedTargets.joinToString { it.name }}!")
                    }
                }
            }
        }
    }

}
