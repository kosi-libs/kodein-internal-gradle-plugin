package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.io.File

public class KodeinLibraryJvmPlugin : KtPlugin<Project> {

    internal companion object {
        const val defaultPublicationName = "libraryMaven"

        private fun addJvmSourcesJar(
            project: Project,
            publication: String = defaultPublicationName,
            sourceSet: Project.() -> Iterable<File> = {
                extensions.getByName<JavaPluginExtension>("java").sourceSets["main"].allSource
            }
        ) {
            val sourcesJar = project.tasks.maybeCreate<Jar>("sourcesJar").apply {
                archiveClassifier.set("sources")
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                from(project.sourceSet())
            }

            project.extensions.getByName<PublishingExtension>("publishing")
                .publications.getByName<MavenPublication>(publication).artifact(sourcesJar)
        }
    }

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinJvmPlugin>()
            plugin("maven-publish")
            plugin("java-library")
            plugin<KodeinUploadModulePlugin>()
        }

        extensions.getByName<KotlinProjectExtension>("kotlin").explicitApi()

        project.extensions.getByName<PublishingExtension>("publishing")
            .publications.create<MavenPublication>(defaultPublicationName) {
                from(components["java"])
            }

        addJvmSourcesJar(project)
    }

}
