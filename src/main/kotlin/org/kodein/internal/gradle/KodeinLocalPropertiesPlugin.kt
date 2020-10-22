package org.kodein.internal.gradle

import org.gradle.api.Project
import java.io.File
import java.util.*

class KodeinLocalPropertiesPlugin : KtPlugin<Project> {

    class KodeinLocalProperties(private val project: Project, internal val local: Properties) {
        operator fun get(key: String): String? =
            local.getProperty(key)
                    ?: (project.properties["org.kodein.local.$key"] as? String)
                    ?: System.getenv("KODEIN_LOCAL_${key.toUpperCase()}")

        fun isTrue(key: String): Boolean = get(key) == "true"

        fun getAsList(key: String): List<String> = get(key)?.split(",")?.map { it.trim() } ?: emptyList()
    }

    override fun Project.applyPlugin() {
        Companion.applyPlugin(project)
    }

    companion object {
        private const val EXTENSION_KEY = "kodeinLocalProperties"

        private fun applyPlugin(project: Project): KodeinLocalProperties = with(project) {
            val rootKLProps = rootProject.extensions.findByName("kodeinLocalProperties") as? KodeinLocalProperties
            if (rootKLProps != null) {
                extensions.add(EXTENSION_KEY, KodeinLocalProperties(project, rootKLProps.local))
                return rootKLProps
            }

            val localProperties = Properties()
            val file = File("$rootDir/kodein.local.properties")
            if (file.exists()) {
                file.inputStream().use {
                    localProperties.load(it)
                }
            }

            val klProps = KodeinLocalProperties(project, localProperties)
            extensions.add(EXTENSION_KEY, klProps)
            return klProps
        }

        fun on(project: Project): KodeinLocalProperties {
            val klProps = project.extensions.findByName(EXTENSION_KEY) as? KodeinLocalProperties
            if (klProps != null) {
                return klProps
            }
            applyPlugin(project)
            return project.extensions.getByName(EXTENSION_KEY) as KodeinLocalProperties
        }
    }

}
