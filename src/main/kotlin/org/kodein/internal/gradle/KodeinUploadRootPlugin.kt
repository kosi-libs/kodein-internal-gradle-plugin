package org.kodein.internal.gradle

import nmcp.NmcpAggregationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import javax.inject.Inject

public class KodeinUploadRootPlugin @Inject constructor(
    private val factory: ObjectFactory
) : Plugin<Project> {

    private lateinit var project: Project

    public inner class PublicationConfig {
        public val snapshot: Boolean = (project.properties["snapshot"] as? String) == "true"

        public val version: String = run {
            val eapBranch = (project.properties["gitRef"] as? String)?.split("/")?.last() ?: "dev"
            if (snapshot) "${project.version}-$eapBranch-SNAPSHOT" else project.version.toString()
        }

        public val githubProjectName: Property<String> = factory.property<String>().convention(project.provider { project.name.ifEmpty { "UNDEFINED" } })
    }

    public val publication: PublicationConfig by lazy { PublicationConfig() }

    public inner class Extension {
        public val githubProjectName: Property<String> get() = publication.githubProjectName
    }

    public class SonatypeConfig(
        public val username: String,
        public val password: String,
        public val dryRun: Boolean
    )

    public val sonatypeConfig: SonatypeConfig? by lazy {
        val username: String? =
            (project.properties["org.kodein.central.portal.username"] as String?) ?: System.getenv("CENTRAL_PORTAL_TOKEN_USERNAME")
        val password: String? =
            (project.properties["org.kodein.central.portal.password"] as String?) ?: System.getenv("CENTRAL_PORTAL_TOKEN_PASSWORD")
        val dryRun: Boolean = (project.properties["org.kodein.central.portal.dryRun"] as String?)?.toBooleanStrict()
            ?: KodeinLocalPropertiesPlugin.on(project).isTrue("central.dryRun")

        when {
            username == null || password == null -> {
                project.logger.warn("$project: Skipping maven publication configuration as the `org.kodein.central.portal.username` or `org.kodein.central.portal.password` property is not defined.")
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

    public class SigningConfig(public val signingKey: String, public val signingPassword: String)

    public val signingConfig: SigningConfig? by lazy {
        val localProps = KodeinLocalPropertiesPlugin.on(project)
        val signingKey: String? = localProps.get("signing.key") ?: System.getenv("GPG_PRIVATE_KEY")
        val signingPassword: String? = localProps.get("signing.password") ?: System.getenv("GPG_PRIVATE_PASSWORD")
        val skipSigning: Boolean = localProps.isTrue("signing.skip")

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

    override fun apply(target: Project) {
        this.project = target
        target.applyPlugin()
    }

    private fun Project.applyPlugin() {
        apply {
            plugin("com.gradleup.nmcp.aggregation")
        }

        extensions.add("kodeinUploadRoot", Extension())

        val nmcp = extensions.getByType<NmcpAggregationExtension>()
        nmcp.centralPortal {
            username.set(provider { sonatypeConfig?.username })
            password.set(provider { sonatypeConfig?.password })
        }
        nmcp.publishAllProjectsProbablyBreakingProjectIsolation()
    }
}
