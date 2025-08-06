package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.gradle.DokkaTask


public class KodeinUploadModulePlugin : KtPlugin<Project> {

    private val Project.publishing get() = extensions.getByName<PublishingExtension>("publishing")
    private val Project.signing get() = extensions.getByName<SigningExtension>("signing")

    internal val disabledPublications = ArrayList<Publication>()
    internal val hostOnlyPublications = ArrayList<Publication>()

    public class Extension {
        public var name: String = ""
        public var description: String = ""
        internal var addJavadoc: Boolean = true
        internal var signPublications: Boolean = true
    }

    override fun Project.applyPlugin() {
        evaluationDependsOn(rootProject.path)

        val root = rootProject.plugins.findPlugin(KodeinUploadRootPlugin::class)
            ?: throw IllegalStateException("Could not find root project's kodeinPublications, have you applied the plugin?")
        val signingConfig = root.signingConfig

        apply {
            plugin("org.jetbrains.dokka")
            plugin("org.gradle.maven-publish")
            if (signingConfig != null) {
                plugin("org.gradle.signing")
            }
        }

        val ext = Extension()
        project.extensions.add("kodeinUpload", ext)

        tasks.withType<AbstractPublishToMaven>().configureEach {
            onlyIf {
                if (publication in disabledPublications) {
                    logger.warn("Publication ${publication.name} disabled")
                    false
                } else true
            }
        }

        afterEvaluate {
            project.version = root.publication.version

            val sonatypeConfig = root.sonatypeConfig?.takeIf {
                if (ext.name.isBlank() || ext.description.isBlank()) {
                    logger.warn(
                        "{}: Skipping sonatype configuration as kodeinUpload has not been configured (empty name and/or description).",
                        project
                    )
                    false
                } else true
            }

            if (sonatypeConfig != null) {
                afterEvaluate {
                    tasks.withType<PublishToMavenRepository>().configureEach {
                        if (repository.name == "nmcp") {
                            onlyIf {
                                logger.warn("${if (sonatypeConfig.dryRun) "DRY RUN " else ""}Packaging '${publication.groupId}:${publication.artifactId}:${publication.version}' from publication '${publication.name}':")
                                val maxSize = inputs.files.maxOf { it.name.length }
                                inputs.files.forEach {
                                    logger.warn(
                                        "    - ${it.name} ${" ".repeat(maxSize - it.name.length)} (${
                                            it.relativeTo(
                                                rootDir
                                            ).path
                                        })"
                                    )
                                }
                                !sonatypeConfig.dryRun
                            }

                            doFirst {
                                val excludeTargets = KodeinLocalPropertiesPlugin.on(project).getAsList("excludeTargets")
                                if (excludeTargets.isNotEmpty()) {
                                    logger.warn("Uploading to OSSRH with excluded targets {}", excludeTargets)
                                }
                            }
                        }
                    }
                }
            }

            val dokkaOutputDir = layout.buildDirectory.dir("dokka")
            tasks.withType<DokkaTask>().configureEach {
                outputDirectory.set(file(dokkaOutputDir))
                dokkaSourceSets {
                    configureEach {
                        perPackageOption {
                            matchingRegex.set(".*\\.internal.*") // will match all .internal packages and sub-packages
                            suppress.set(true)
                        }
                    }
                }
            }

            if (ext.addJavadoc) {
                // Workaround from https://youtrack.jetbrains.com/issue/KT-46466 & https://github.com/gradle/gradle/issues/26091
                publishing.publications.withType<MavenPublication>().configureEach {
                    val publication = this
                    val javadocJar = tasks.maybeCreate<Jar>("${publication.name}JavadocJar").apply {
                        dependsOn("dokkaGenerate")
                        archiveClassifier.set("javadoc")
                        from(dokkaOutputDir)
                        // Each archive name should be distinct. Mirror the format for the sources Jar tasks.
                        archiveBaseName.set("${archiveBaseName.get()}-${publication.name}")
                    }
                    artifact(javadocJar)
                }
            }

            publishing.publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set(ext.name)
                    description.set(ext.description)
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    url.set("http://kodein.org")
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/kosi-libs/${root.publication.projectName}/issues")
                    }
                    scm {
                        connection.set("https://github.com/kosi-libs/${root.publication.projectName}.git")
                        url.set("https://github.com/kosi-libs/${root.publication.projectName}")
                    }
                    developers {
                        developer {
                            name.set("Kodein Koders")
                            email.set("dev@kodein.net")
                        }
                    }
                }
            }

            tasks.withType<GenerateModuleMetadata>().configureEach {
                onlyIf {
                    publication.get() !in disabledPublications
                }
            }

            tasks.register("hostOnlyPublish") {
                group = "publishing"
                // Configuration must NOT be on-demand here.
                dependsOn(tasks.withType<PublishToMavenRepository>().filter { it.publication in hostOnlyPublications })
            }

            if (signingConfig != null) {
                signing.apply { useInMemoryPgpKeys(signingConfig.signingKey, signingConfig.signingPassword) }
                if (ext.signPublications) {
                    // Workaround from https://youtrack.jetbrains.com/issue/KT-46466 & https://github.com/gradle/gradle/issues/26091
                    publishing.publications.configureEach { signing.sign(this) }
                }
            }
        }
    }
}
