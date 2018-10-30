package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.repositories

class KodeinRootPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin<KodeinPublicationsPlugin>()
        }

        allprojects {
            repositories {
                jcenter()
                google()
                maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
            }
        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
