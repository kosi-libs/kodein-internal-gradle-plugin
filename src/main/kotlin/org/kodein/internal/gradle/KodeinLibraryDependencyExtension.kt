package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.publish.maven.MavenPublication
import org.w3c.dom.Element
import org.w3c.dom.NodeList

@Suppress("UnstableApiUsage")
class KodeinLibraryDependencyExtension(val project: Project) {

    private val deps = HashMap<ProjectDependency, String>()

    infix fun ProjectDependency.target(target: String): ProjectDependency {
        deps[this] = target
        return this
    }

    @Suppress("UnstableApiUsage")
    internal fun updatePom(publication: MavenPublication) {
        publication.pom.withXml {
            val doc = asElement().ownerDocument

            val dependencies = doc.firstChild.childNodes.seq<Element>().firstOrNull { it.tagName == "dependencies" }

            dependencies?.getElementsByTagName("dependency")?.seq<Element>()?.forEach { el ->
                val group = el.getElementsByTagName("groupId").item(0).textContent
                val name = el.getElementsByTagName("artifactId").item(0).textContent
                val version = el.getElementsByTagName("version").item(0).textContent

                deps.filter { it.key.group == group && it.key.name == name && it.key.version == version }.forEach {
                    el.getElementsByTagName("artifactId").item(0).textContent = it.key.name + "-" + it.value
                }
            }
        }
    }

    private inline fun <reified T> NodeList.seq() = (0 until length).asSequence().map { item(it) } .filterIsInstance<T>()

}
