package org.kodein.internal.gradle

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayUploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*


class KodeinPublicationExtension(val project: Project) {

    var publications: () -> Array<out Any> = { arrayOf("Kodein") }

    class UploadData {
        var name: String = ""
        var description: String = ""
        var repo: String = ""
    }

    fun upload(block: UploadData.() -> Unit) = project.run {
        if (!hasProperty("bintrayUsername") || !hasProperty("bintrayApiKey")) {
            logger.warn("$project: Ignoring bintrayUpload in because bintrayUsername and/or bintrayApiKey is not set in gradle.properties.")
            return
        }

        val data = UploadData().also(block)

        val bintrayUsername: String by project
        val bintrayApiKey: String by project

        if (data.name.isEmpty() || data.description.isEmpty() || data.repo.isEmpty()) {
            logger.warn("$project: Cannot configure bintrayUpload because upload data has not been configured (empty name, description and/or repo).")
            return
        }

        extensions.configure<BintrayExtension>("bintray") {
            user = bintrayUsername
            key = bintrayApiKey

            pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
                userOrg = "kodein-framework"
                repo = data.repo
                name = data.name
                setLicenses("MIT")
                websiteUrl = "http://kodein.org"
                issueTrackerUrl = "https://github.com/Kodein-Framework/${data.repo}/issues"
                vcsUrl = "https://github.com/Kodein-Framework/${data.repo}.git"
                desc = data.description

//                setPublications(*extension.publications())
            })
        }

    }
}

class KodeinPublicationUpload : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply { plugin("com.jfrog.bintray") }

        val extension = KodeinPublicationExtension(this)
        project.extensions.add("kodeinPublication", extension)

        (tasks["bintrayUpload"] as BintrayUploadTask).doFirst {
            (this as BintrayUploadTask).setPublications(*extension.publications())
        }
    }

    override fun apply(project: Project) = project.applyPlugin()
}
