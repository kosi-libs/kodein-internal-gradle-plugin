package org.kodein.internal.gradle

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

data class KodeinPublicationExtension(
    var name: String = "",
    var description: String = "",
    var repo: String = ""
)

class KodeinPublicationUpload : Plugin<Project> {

    private fun Project.applyPlugin() {
        val extension = KodeinPublicationExtension()
        project.extensions.add("kodeinPublication", extension)

        if (!hasProperty("bintrayUsername") || !hasProperty("bintrayApiKey")) {
            logger.warn("$project: Ignoring bintrayUpload in because bintrayUsername and/or bintrayApiKey is not set in gradle.properties.")
            return
        }

        val bintrayUsername: String by project
        val bintrayApiKey: String by project

        apply { plugin("com.jfrog.bintray") }

        afterEvaluate {
            if (extension.name.isEmpty() || extension.description.isEmpty() || extension.repo.isEmpty()) {
                logger.warn("$project: Cannot configure bintrayUpload because kodeinPublication has not been configured (empty name, description and/or repo).")
                return@afterEvaluate
            }

            extensions.configure<BintrayExtension>("bintray") {
                user = bintrayUsername
                key = bintrayApiKey

                pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
                    userOrg = "kodein-framework"
                    repo = extension.repo
                    name = extension.name
                    setLicenses("MIT")
                    websiteUrl = "http://kodein.org"
                    issueTrackerUrl = "https://github.com/Kodein-Framework/${extension.repo}/issues"
                    vcsUrl = "https://github.com/Kodein-Framework/${extension.repo}.git"
                    desc = extension.description

                    setPublications("Kodein")
                })
            }
        }
    }

    override fun apply(project: Project) = project.applyPlugin()
}
