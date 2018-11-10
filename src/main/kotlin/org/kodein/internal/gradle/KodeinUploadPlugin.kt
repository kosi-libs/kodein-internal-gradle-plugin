package org.kodein.internal.gradle

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayUploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import java.lang.IllegalStateException


@Suppress("UnstableApiUsage")
class KodeinUploadPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply { plugin("com.jfrog.bintray") }

        val bintray = extensions["bintray"] as BintrayExtension
        project.extensions.add(KodeinUploadExtension::class.java, "kodeinUpload", KodeinBintrayUploadExtension(bintray))

        evaluationDependsOn(rootProject.path)

        val rootExt = rootProject.extensions.findByName("kodeinPublications") as? KodeinPublicationsExtension
                ?: throw IllegalStateException("Could not find root project's kodeinPublications, have you applied the plugin?")

        if (rootExt.repo.isEmpty()) {
            logger.warn("$project: Cannot configure bintray upload because root project's kodeinPublications has not been configured (empty repo).")
            return
        }

        if (!hasProperty("bintrayUsername") || !hasProperty("bintrayApiKey")) {
            logger.warn("$project: Ignoring bintrayUpload in because bintrayUsername and/or bintrayApiKey is not set in gradle.properties.")
            return
        }

        val bintrayUsername: String by project
        val bintrayApiKey: String by project
        val bintrayUserOrg: String? by project
        val bintrayDryRun: String? by project

        bintray.apply {
            user = bintrayUsername
            key = bintrayApiKey
            dryRun = bintrayDryRun == "true"

            pkg.apply {
                if (bintrayUserOrg != null)
                    userOrg = bintrayUserOrg
                repo = rootExt.repo
                setLicenses("MIT")
                websiteUrl = "http://kodein.org"
                issueTrackerUrl = "https://github.com/Kodein-Framework/${rootExt.repo}/issues"
                vcsUrl = "https://github.com/Kodein-Framework/${rootExt.repo}.git"
            }
        }

        val uploadTask = tasks["bintrayUpload"]
        uploadTask.doFirst {
            if (bintray.pkg.name.isNullOrBlank() || bintray.pkg.desc.isNullOrBlank()) {
                throw IllegalStateException("$project: Cannot configure bintray upload because kodeinUpload has not been configured (empty name and/or description).")
            }

            this as BintrayUploadTask
            val publications = project.extensions.getByName<PublishingExtension>("publishing").publications.filter { "-test" !in it.name }
            publications.filterIsInstance<MavenPublication>().forEach {
                logger.warn("${if (bintrayDryRun == "true") "DRY RUN " else ""} Uploading artifact '${it.groupId}:${it.artifactId}:${it.version}' from publication '${it.name}'")
            }
            setPublications(*publications.toTypedArray())
        }

        uploadTask.dependsOn("publishToMavenLocal")
    }

    override fun apply(project: Project) = project.applyPlugin()

}
