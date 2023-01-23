package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate

public class KodeinUploadRootPlugin : Plugin<Project> {

    private lateinit var project: Project

    public inner class PublicationConfig {
        private val repositoryId = (project.properties["org.kodein.sonatype.repositoryId"] as String?) ?: System.getenv("SONATYPE_REPOSITORY_ID")
        internal val snapshot: Boolean = (project.properties["snapshot"] as? String) == "true"

        public val repositoryUrl: String by lazy {
            when {
                snapshot -> "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                repositoryId != null -> "https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$repositoryId/"
                else -> error("Cannot publish to OSSRH as the default url would end up creating a lot of staging repositories.")
            }
        }

        public val version: String = run {
            val eapBranch = (project.properties["gitRef"] as? String)?.split("/")?.last() ?: "dev"
            if (snapshot) "${project.version}-$eapBranch-SNAPSHOT" else project.version.toString()
        }

        public val projectName: String = project.name.ifEmpty { "UNDEFINED" }
    }

    public val publication: PublicationConfig by lazy { PublicationConfig() }

    public inner class SonatypeConfig(
            public val username: String,
            public val password: String,
            public val dryRun: Boolean
    )
    public val sonatypeConfig: SonatypeConfig? by lazy {
        val username: String? = (project.properties["org.kodein.sonatype.username"] as String?) ?: System.getenv("SONATYPE_USERNAME")
        val password: String? = (project.properties["org.kodein.sonatype.password"] as String?) ?: System.getenv("SONATYPE_PASSWORD")
        val dryRun: Boolean = (project.properties["org.kodein.sonatype.dryRun"] as Boolean?) ?: KodeinLocalPropertiesPlugin.on(project).isTrue("ossrh.dryRun")

        when {
            username == null || password == null -> {
                project.logger.warn("$project: Skipping maven publication configuration as the `org.kodein.sonatype.username` or `org.kodein.sonatype.password` property is not defined.")
                null
            }
            else -> {
                SonatypeConfig(
                        username = username,
                        password = password,
                        dryRun = dryRun
                )
            }
        }
    }

    public inner class SigningConfig(public val signingKey: String, public val signingPassword: String)
    public val signingConfig: SigningConfig? by lazy {
        val signingKey: String? = (project.properties["org.kodein.signing.key"] as String?) ?: System.getenv("GPG_PRIVATE_KEY")
        val signingPassword: String? = (project.properties["org.kodein.signing.password"] as String?) ?: System.getenv("GPG_PRIVATE_PASSWORD")
        val skipSigning: Boolean = (KodeinLocalPropertiesPlugin.on(project).isTrue("org.kodein.signing.skip"))

        when {
            signingKey == null || signingPassword == null || skipSigning || publication.snapshot -> {
                project.logger.warn(
                    buildString {
                        append("$project: Skipping signing publication configuration")
                        if (skipSigning || publication.snapshot)
                            append(" either because of the defined parameter: `org.kodein.signing.skip = true` or publishing a snapshot.")
                        else
                            append(" as the `org.kodein.signing.key` or `org.kodein.signing.password` property is not defined.")
                    }
                )
                null
            }
            else -> {
                SigningConfig(
                    signingKey = signingKey,
                    signingPassword = signingPassword
                )
            }
        }
    }

    override fun apply(project: Project) {
        this.project = project
    }

}
