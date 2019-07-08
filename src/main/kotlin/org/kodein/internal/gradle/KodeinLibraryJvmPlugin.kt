package org.kodein.internal.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

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
            archiveClassifier.set("sources")
            setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
        }

        afterEvaluate {
            val javaPlugin = project.convention.getPluginByName<org.gradle.api.plugins.JavaPluginConvention>("java")
            val sourceSets = javaPlugin.sourceSets
            sourcesJar.from(sourceSets["main"].allSource)

            javaPlugin.sourceCompatibility = JavaVersion.VERSION_1_8
            javaPlugin.targetCompatibility = JavaVersion.VERSION_1_8

            extensions.configure<PublishingExtension>("publishing") {
                publications {
                    create<MavenPublication>("Kodein") {
                        from(components["java"])
                        artifact(sourcesJar)

                        ext.updatPom(this)
                    }
                }
            }
        }
    }

}
