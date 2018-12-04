package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.lang.IllegalStateException

class KodeinLibraryMppExtension(val project: Project) {

    inner class Conf {
        val baseArtifactName: String = project.name
        val platformCorrespondence: MutableMap<String, String> = HashMap()
    }

    @Suppress("UnstableApiUsage")
    fun updateMavenPom(block: Conf.() -> Unit = {}) {
        val conf = Conf().apply(block)

        val kotlin = project.extensions["kotlin"] as KotlinMultiplatformExtension
        val deps = with(kotlin.sourceSets["commonMain"]) { listOf(apiMetadataConfigurationName, implementationMetadataConfigurationName, compileOnlyMetadataConfigurationName, runtimeOnlyMetadataConfigurationName) }
                .map { project.configurations[it] }
                .flatMap { it.allDependencies }
                .filterIsInstance<ProjectDependency>()
                .distinct()

        val publishing = project.extensions["publishing"] as PublishingExtension
        publishing.publications.filterIsInstance<MavenPublication>().forEach { pub ->
            pub.pom.withXml {
                val doc = asElement().ownerDocument

                val artifactId = doc.firstChild.childNodes.seq<Element>().first { it.tagName == "artifactId" }.textContent
                if (!artifactId.startsWith(conf.baseArtifactName)) {
                    throw IllegalStateException("Artifact $artifactId does not start with base name ${conf.baseArtifactName}")
                }
                if (artifactId.length == conf.baseArtifactName.length) {
                    return@withXml
                }

                val artifactPlatform = artifactId.substring(conf.baseArtifactName.length + 1)
                val correspondingPlatform = conf.platformCorrespondence[artifactPlatform] ?: artifactPlatform

                val dependencies = doc.firstChild.childNodes.seq<Element>().firstOrNull { it.tagName == "dependencies" }

                dependencies?.getElementsByTagName("dependency")?.seq<Element>()?.forEach { el ->
                    val group = el.getElementsByTagName("groupId").item(0).textContent
                    val name = el.getElementsByTagName("artifactId").item(0).textContent
                    val version = el.getElementsByTagName("version").item(0).textContent

                    deps.filter { it.group == group && it.name == name && it.version == version }.forEach {
                        el.getElementsByTagName("artifactId").item(0).textContent = it.name + "-" + correspondingPlatform
                    }
                }
            }
        }
    }

    private inline fun <reified T> NodeList.seq() = (0 until length).asSequence().map { item(it) } .filterIsInstance<T>()

}
