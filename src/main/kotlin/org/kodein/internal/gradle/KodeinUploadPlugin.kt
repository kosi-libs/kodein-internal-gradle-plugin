package org.kodein.internal.gradle

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.HttpClients
import org.gradle.api.*
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.internal.impldep.org.apache.maven.model.License
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import java.net.URI
import java.net.http.HttpClient


@Suppress("UnstableApiUsage")
class KodeinUploadPlugin : KtPlugin<Project> {

    private val Project.publishing get() = extensions.getByName<PublishingExtension>("publishing")
    private fun Project.publishing(action: PublishingExtension.() -> Unit) = publishing.apply(action)

    internal val disabledPublications = ArrayList<Publication>()

    override fun Project.applyPlugin() {
        apply { plugin("org.gradle.maven-publish") }

        val ext = KodeinBintrayUploadExtension()
        project.extensions.add(KodeinUploadExtension::class.java, "kodeinUpload", ext)
        evaluationDependsOn(rootProject.path)

        val rootExt = rootProject.extensions.findByName("kodeinPublications") as? KodeinPublicationsExtension
                ?: throw IllegalStateException("Could not find root project's kodeinPublications, have you applied the plugin?")

        if (rootExt.repo.isEmpty()) {
            logger.warn("$project: Cannot configure bintray upload because root project's kodeinPublications has not been configured (empty repo).")
            return
        }

        val bintrayUsername: String? = (properties["bintrayUsername"] as String?) ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey: String? = (properties["bintrayApiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")
        val bintrayUserOrg: String? = (properties["bintrayUserOrg"] as String?) ?: System.getenv("BINTRAY_USER_ORG")
        val bintrayDryRun: String? by project
        val snapshotNumber: String? by project

        if (bintrayUsername == null || bintrayApiKey == null) {
            logger.warn("$project: Ignoring bintrayUpload in because bintrayUsername and/or bintrayApiKey is not set in gradle.properties.")
            return
        }

        afterEvaluate {
            if (snapshotNumber != null) project.version = "${project.version}-dev-$snapshotNumber"
            val btSubject = bintrayUserOrg ?: bintrayUsername
            val btRepo = if (snapshotNumber != null) "kodein-dev" else rootExt.repo
            val btDryRun = bintrayDryRun == "true"

            publishing {
                repositories {
                    maven {
                        name = "bintray"
                        val isSnaphost = if (snapshotNumber != null) 1 else 0
                        setUrl("https://api.bintray.com/maven/$btSubject/$btRepo/${ext.name}/;publish=0")
                        credentials {
                            username = bintrayUsername
                            password = bintrayApiKey
                        }
                    }
                }
            }

            tasks["publishAllPublicationsToBintrayRepository"].doFirst {
                if (project.findProperty("classpathFixes") != null) {
                    error("Cannot publish to Bintray with classpath fixes!")
                }
                val excludeTargets = project.findProperty("excludeTargets")
                if (excludeTargets != null) {
                    logger.warn("UPLOADING TO BINTRAY WITH EXCLUDED TARGETS $excludeTargets")
                }

                if (ext.name.isEmpty() || ext.description.isEmpty()) {
                    error("$project: Cannot configure bintray upload because kodeinUpload has not been configured (empty name and/or description).")
                }
            }

            val createPackage = tasks.maybeCreate("create${ext.name.capitalize()}PackageToBintrayRepository").apply {
                onlyIf {
                    !btDryRun and run {
                        HttpClients.createDefault().use { client ->
                            client.execute(HttpGet("https://api.bintray.com/packages/$btSubject/$btRepo/${ext.name}")).use {
                                it.statusLine.statusCode == 404
                            }
                        }
                    }
                }
                doLast {
                    val json = Gson().toJson(mapOf(
                            "name" to ext.name,
                            "desc" to ext.description,
                            "licenses" to arrayOf("MIT"),
                            "vcs_url" to "https://github.com/Kodein-Framework/${rootExt.repo}.git",
                            "website_url" to "http://kodein.org",
                            "issue_tracker_url" to "https://github.com/Kodein-Framework/${rootExt.repo}/issues"
                    ))
                    HttpClients.createDefault().use { client ->
                        val post = HttpPost("https://api.bintray.com/packages/$btSubject/$btRepo").apply {
                            entity = StringEntity(json, ContentType.APPLICATION_JSON)
                            addHeader(BasicScheme().authenticate(UsernamePasswordCredentials(bintrayUsername, bintrayApiKey), this, null))
                        }
                        client.execute(post).use {
                            check(it.statusLine.statusCode == 201) {
                                "Could not create package (HTTP status code ${it.statusLine.statusCode}): " + it.entity.content.reader().readText()
                            }
                        }
                    }
                }
            }
            publishing.publications.withType<MavenPublication>().configureEach {
                pom {
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
                        url.set("https://github.com/Kodein-Framework/${rootExt.repo}/issues")
                    }
                    scm {
                        connection.set("https://github.com/Kodein-Framework/${rootExt.repo}.git")
                    }
                }
            }

            afterEvaluate {
                tasks.withType<PublishToMavenRepository>().configureEach {
                    if (this.repository.name == "bintray") {
                        dependsOn(createPackage)
                        onlyIf {
                            if (publication in disabledPublications) {
                                logger.warn("Publication ${publication.name} disabled")
                                false
                            } else {
                                logger.warn("${if (btDryRun) "DRY RUN " else ""}Uploading '${publication.groupId}:${publication.artifactId}:${publication.version}' from publication '${publication.name}':")
                                inputs.files.forEach {
                                    logger.warn("    - " + it.name)
                                }
                                !btDryRun
                            }
                        }
                    }
                }

                tasks.withType<GenerateModuleMetadata>().configureEach {
                    onlyIf {
                        publication.get() !in disabledPublications
                    }
                }
            }
        }
    }

}
