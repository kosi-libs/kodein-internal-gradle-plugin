package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial


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
            plugin("org.jetbrains.dokka")
            plugin("org.gradle.maven-publish")
            plugin("org.gradle.signing")
        }

        val ext = Extension()
        project.extensions.add("kodeinUpload", ext)
        evaluationDependsOn(rootProject.path)

        tasks.withType<AbstractPublishToMaven>()
            .configureEach {
                onlyIf {
                    if (publication in disabledPublications) {
                        logger.warn("Publication ${publication.name} disabled")
                        false
                    } else true
                }
            }

        afterEvaluate {

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
                        .configureEach {
                            if (repository.name == "ossrhStaging") {
                                onlyIf {
                                    logger.warn("${if (sonatypeConfig.dryRun) "DRY RUN " else ""}Uploading '${publication.groupId}:${publication.artifactId}:${publication.version}' from publication '${publication.name}':")
                                    val maxSize = inputs.files.maxOf { it.name.length }
                                    inputs.files.forEach {
                                        logger.warn("    - ${it.name} ${" ".repeat(maxSize - it.name.length)} (${it.relativeTo(rootDir).path})")
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
            }

            val dokkaOutputDir = buildDir.resolve("dokka")
            tasks.withType<DokkaTask>().configureEach {
                outputDirectory.set(file(dokkaOutputDir))
                dokkaSourceSets {
                    configureEach {
                        val platformName = when(platform.get()) {
                            Platform.jvm -> "jvm"
                            Platform.js -> "js"
                            Platform.native -> "native"
                            Platform.common -> "common"
                        }
                        displayName.set(platformName)

                        perPackageOption {
                            matchingRegex.set(".*\\.internal.*") // will match all .internal packages and sub-packages
                            suppress.set(true)
                        }
                    }
                }
            }

            // https://github.com/Kotlin/dokka/issues/1455
            tasks.withType<DokkaTask>().configureEach { dependsOn("assemble") }
            tasks.withType<DokkaTaskPartial>().configureEach { dependsOn("assemble") }

            val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
                delete(dokkaOutputDir)
            }
            tasks.named("dokkaHtml").configure { dependsOn(deleteDokkaOutputDir) }

            val javadocJar = tasks.register<Jar>("javadocJar") {
                dependsOn("dokkaHtml")
                archiveClassifier.set("javadoc")
                from(dokkaOutputDir)
            }

            tasks.named("dokkaGfm") { dependsOn("build") }
            tasks.named("dokkaGfmPartial") { dependsOn("build") }
            tasks.named("dokkaHtml") { dependsOn("build") }
            tasks.named("dokkaHtmlPartial") { dependsOn("build") }
            tasks.named("dokkaJavadoc") { dependsOn("build") }
            tasks.named("dokkaJavadocPartial") { dependsOn("build") }
            tasks.named("dokkaJekyll") { dependsOn("build") }
            tasks.named("dokkaJekyllPartial") { dependsOn("build") }

            project.version = root.publication.version
            publishing.publications.withType<MavenPublication>().configureEach {
                if (!name.endsWith("PluginMarkerMaven")) {
                    val artifactJavadoc = tasks.maybeCreate<Copy>("${artifactId}JavadocJar").apply {
                        dependsOn(javadocJar)
                        from(javadocJar)
                        into("$buildDir/tmp/javadocJars/$artifactId")
                    }
                    artifact("$buildDir/tmp/javadocJars/$artifactId/${javadocJar.get().archiveFileName.get()}") {
                        builtBy(artifactJavadoc)
                        classifier = "javadoc"
                    }
                }
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
