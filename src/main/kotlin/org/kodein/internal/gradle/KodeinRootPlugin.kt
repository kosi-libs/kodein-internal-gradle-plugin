package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.repositories

class KodeinRootPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinUploadRootPlugin>()
        }

        allprojects {
            repositories {
                mavenLocal()
                jcenter()
                google()
                maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
                maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
            }
        }
    }

}
