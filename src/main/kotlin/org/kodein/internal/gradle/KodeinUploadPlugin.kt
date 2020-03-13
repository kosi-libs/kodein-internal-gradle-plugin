package org.kodein.internal.gradle

import com.jfrog.bintray.gradle.*
import com.jfrog.bintray.gradle.tasks.*
import org.gradle.api.*
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*


@Suppress("UnstableApiUsage")
class KodeinUploadPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
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

        val bintrayUsername = (properties["bintrayUsername"] as String?) ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = (properties["bintrayApiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")
        val bintrayUserOrg = (properties["bintrayUserOrg"] as String?) ?: System.getenv("BINTRAY_USER_ORG")
        val bintrayDryRun: String? by project
        val snapshotNumber: String? by project

        if (bintrayUsername == null || bintrayApiKey == null) {
            logger.warn("$project: Ignoring bintrayUpload in because bintrayUsername and/or bintrayApiKey is not set in gradle.properties.")
            return
        }

        bintray.apply {
            user = bintrayUsername
            key = bintrayApiKey
            dryRun = bintrayDryRun == "true"

            override = snapshotNumber != null
            publish = snapshotNumber != null

            pkg.apply {
                if (bintrayUserOrg != null)
                    userOrg = bintrayUserOrg
                repo = rootExt.repo
                setLicenses("MIT")
                websiteUrl = "http://kodein.org"
                issueTrackerUrl = "https://github.com/Kodein-Framework/${rootExt.repo}/issues"
                vcsUrl = "https://github.com/Kodein-Framework/${rootExt.repo}.git"

                if (snapshotNumber != null) {
                    repo = "kodein-dev"
                    project.version = "${project.version}-dev-$snapshotNumber"
                    publish = true
                }
            }
        }

        val bintrayUploadTask = tasks["bintrayUpload"] as BintrayUploadTask

        bintrayUploadTask.apply {
            dependsOn("publishToMavenLocal")
            doFirst {
                if (project.findProperty("classpathFixes") != null) {
                    error("Cannot use bintrayUpload with Classpath Fixes!")
                }
                val excludeTargets = project.findProperty("excludeTargets")
                if (excludeTargets != null) {
                    logger.warn("UPLOADING TO BINTRAY WITH EXCLUDED TARGETS $excludeTargets")
                }

                if (bintray.pkg.name.isNullOrBlank() || bintray.pkg.desc.isNullOrBlank()) {
                    throw IllegalStateException("$project: Cannot configure bintray upload because kodeinUpload has not been configured (empty name and/or description).")
                }

                val publications = project.extensions.getByName<PublishingExtension>("publishing").publications.filter { "-test" !in it.name }
                publications.filterIsInstance<MavenPublication>().forEach {
                    logger.warn("${if (bintrayDryRun == "true") "DRY RUN " else ""} Uploading artifact '${it.groupId}:${it.artifactId}:${it.version}' from publication '${it.name}'")
                }
                setPublications(*publications.map { it.name } .toTypedArray())
            }

            bintrayUploadTask.dependsOn("publishToMavenLocal")

            doFirst {
                if (project.findProperty("classpathFixes") != null) {
                    error("Cannot use bintrayUpload with Classpath Fixes!")
                }
                val excludeTargets = project.findProperty("excludeTargets")
                if (excludeTargets != null) {
                    logger.warn("UPLOADING TO BINTRAY WITH EXCLUDED TARGETS $excludeTargets")
                }
            }
        }
    }

}
