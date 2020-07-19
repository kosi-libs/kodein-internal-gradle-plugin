package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate

class KodeinPublicationsPlugin : Plugin<Project> {

    private lateinit var project: Project

    private val ext = KodeinPublicationsExtension()

    inner class PublicationConfig {
        val snapshotNumber = project.properties["snapshotNumber"] as? String
        val version = run {
            val eapBranch = (project.properties["gitRef"] as? String)?.split("/")?.last() ?: "dev"
            if (snapshotNumber != null) "${project.version}-$eapBranch-$snapshotNumber" else project.version.toString()
        }
        val repoName = ext.repo.ifEmpty { "UNDEFINED" }
    }

    val publication by lazy { PublicationConfig() }

    inner class BintrayConfig(
            val username: String,
            val apiKey: String,
            val userOrg: String?,
            val dryRun: Boolean
    ) {
        val subject = userOrg ?: username
        val repo = if (publication.snapshotNumber != null) "kodein-dev" else publication.repoName
    }

    val bintray: BintrayConfig? by lazy {
        val bintrayUsername: String? = (project.properties["bintrayUsername"] as String?) ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey: String? = (project.properties["bintrayApiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")

        when {
            bintrayUsername == null || bintrayApiKey == null -> {
                project.logger.warn("$project: Skipping bintray configuration as the bintrayUsername or bintrayApiKey property is not defined.")
                null
            }
            ext.repo.isEmpty() -> {
                project.logger.warn("$project: Skipping bintray configuration as root project's kodeinPublications has not been configured (empty repo).")
                null
            }
            else -> {
                BintrayConfig(
                        username = bintrayUsername,
                        apiKey = bintrayApiKey,
                        userOrg = (project.properties["bintrayUserOrg"] as String?) ?: System.getenv("BINTRAY_USER_ORG"),
                        dryRun = project.properties["bintrayDryRun"] == "true"
                )
            }
        }
    }

    override fun apply(project: Project) {
        this.project = project
        project.extensions.add("kodeinPublications", ext)
    }

}
