package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin

@Suppress("UnstableApiUsage")
class KodeinLibraryJvmPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinJvmPlugin>()
            plugin("maven-publish")
            plugin("java-library")
            plugin<KodeinUploadPlugin>()
        }

        val ext = KodeinLibraryDependencyExtension(this)
        extensions.add("kodeinLib", ext)

        val sourcesJar = task<Jar>("sourcesJar") {
            classifier = "sources"
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        afterEvaluate {
            val sourceSets = project.convention.getPluginByName<org.gradle.api.plugins.JavaPluginConvention>("java").sourceSets
            sourcesJar.from(sourceSets["main"].allSource)

            extensions.configure<PublishingExtension>("publishing") {
                (publications) {
                    "Kodein"(MavenPublication::class) {
                        from(components["java"])
                        artifact(sourcesJar)

                        ext.updatPom(this)
                    }
                }
            }
        }

        printTestLogs()
    }

}
