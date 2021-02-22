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
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import kotlin.collections.ArrayList


@Suppress("UnstableApiUsage")
class KodeinUploadModulePlugin : KtPlugin<Project> {

    private val Project.publishing get() = extensions.getByName<PublishingExtension>("publishing")
    private val Project.signing get() = extensions.getByName<SigningExtension>("signing")

    internal val disabledPublications = ArrayList<Publication>()
    internal val hostOnlyPublications = ArrayList<Publication>()

    class Extension {
        var name: String = ""
        var description: String = ""
    }

    override fun Project.applyPlugin() {
        apply {
            plugin("org.gradle.maven-publish")
            plugin("org.gradle.signing")
        }

        val ext = Extension()
        project.extensions.add("kodeinUpload", ext)
        evaluationDependsOn(rootProject.path)

        afterEvaluate {
            tasks.withType<AbstractPublishToMaven>()
                    .applyEach {
                        onlyIf {
                            if (publication in disabledPublications) {
                                logger.warn("Publication ${publication.name} disabled")
                                false
                            } else true
                        }
                    }

            val root = rootProject.plugins.findPlugin(KodeinUploadRootPlugin::class)
                    ?: throw IllegalStateException("Could not find root project's kodeinPublications, have you applied the plugin?")

            val sonatypeConfig = root.sonatypeConfig?.takeIf {
                if (ext.name.isBlank() || ext.description.isBlank()) {
                    logger.warn("$project: Skipping sonatype configuration as kodeinUpload has not been configured (empty name and/or description).")
                    false
                } else true
            }

            if (sonatypeConfig != null) {
                publishing.repositories {
                    maven {
                        name = "ossrhStaging"
                        setUrl(provider { root.publication.repositoryUrl })
                        credentials {
                            username = sonatypeConfig.username
                            password = sonatypeConfig.password
                        }
                    }
                }

                afterEvaluate {
                    tasks.withType<PublishToMavenRepository>()
                        .filter { it.repository.name == "ossrhStaging" }
                        .applyEach {
                            onlyIf {
                                logger.warn("${if (sonatypeConfig.dryRun) "DRY RUN " else ""}Uploading '${publication.groupId}:${publication.artifactId}:${publication.version}' from publication '${publication.name}':")
                                inputs.files.forEach {
                                    logger.warn("    - " + it.name)
                                }
                                !sonatypeConfig.dryRun
                            }

                            doFirst {
                                if (KodeinLocalPropertiesPlugin.on(project).getAsList("classpathFixes").isNotEmpty()) {
                                    error("Cannot publish to OSSRH with classpath fixes!")
                                }
                                val excludeTargets = KodeinLocalPropertiesPlugin.on(project).getAsList("excludeTargets")
                                if (excludeTargets.isNotEmpty()) {
                                    logger.warn("Uploading to OSSRH with excluded targets $excludeTargets")
                                }
                            }
                        }
                }
            }

            // Empty javadoc ; TODO replace with Dokka
            val javadocJar = tasks.register("javadocJar", Jar::class.java) {
                archiveClassifier.set("javadoc")
            }

            project.version = root.publication.version
            publishing.publications.withType<MavenPublication>().configureEach {
                artifact(javadocJar)
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
                        url.set("https://github.com/Kodein-Framework/${root.publication.projectName}/issues")
                    }
                    scm {
                        connection.set("https://github.com/Kodein-Framework/${root.publication.projectName}.git")
                        url.set("https://github.com/Kodein-Framework/${root.publication.projectName}")
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

            tasks.create("hostOnlyPublish") {
                group = "publishing"
                tasks.withType<PublishToMavenRepository>()
                        .filter { it.publication in hostOnlyPublications }
                        .forEach {
                            dependsOn(it)
                        }
            }

            val signingConfig = root.signingConfig
            if(signingConfig != null ) {
                signing.apply {
                    useInMemoryPgpKeys(signingConfig.signingKey, signingConfig.signingPassword)
                    sign(publishing.publications)
                }
            }
        }

    }

}
