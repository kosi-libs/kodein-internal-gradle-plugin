package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

@Suppress("UnstableApiUsage")
class KodeinLibraryJvmPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinJvmPlugin>()
            plugin("maven-publish")
            plugin("java-library")
            plugin<KodeinUploadPlugin>()
        }

        extensions.getByName<KotlinProjectExtension>("kotlin").explicitApi()

        val ext = KodeinLibraryDependencyExtension(this)
        extensions.add("kodeinLib", ext)

        val sourcesJar = task<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
        }

        afterEvaluate {
            val javaPlugin = project.convention.getPluginByName<org.gradle.api.plugins.JavaPluginConvention>("java")
            val sourceSets = javaPlugin.sourceSets
            sourcesJar.from(sourceSets["main"].allSource)

            extensions.configure<PublishingExtension>("publishing") {
                publications {
                    create<MavenPublication>("Kodein") {
                        from(components["java"])
                        artifact(sourcesJar)

                        ext.updatePom(this)
                    }
                }
            }
        }
    }

}
