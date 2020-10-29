package org.kodein.internal.gradle

import com.google.gson.Gson
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import java.util.concurrent.TimeUnit


@Suppress("UnstableApiUsage")
class KodeinUploadModulePlugin : KtPlugin<Project> {

    private val Project.publishing get() = extensions.getByName<PublishingExtension>("publishing")

    internal val disabledPublications = ArrayList<Publication>()
    internal val hostOnlyPublications = ArrayList<Publication>()

    class Extension {
        var name: String = ""
        var description: String = ""
        var packageOf: String? = null
    }

    override fun Project.applyPlugin() {
        apply { plugin("org.gradle.maven-publish") }

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

            val bintray = root.bintray?.takeIf {
                if (ext.name.isBlank() || ext.description.isBlank()) {
                    logger.warn("$project: Skipping bintray configuration as kodeinUpload has not been configured (empty name and/or description).")
                    false
                } else true
            }

            if (bintray != null) {
                val pkg =
                    if (ext.packageOf != null) {
                        val parentExt = project(ext.packageOf!!).extensions.findByName("kodeinUpload") as? Extension
                            ?: error("Could not find kodeinUpload extension in project ${ext.packageOf}")
                        if (parentExt.packageOf != null) error("Project ${ext.packageOf} cannot be used as parent package because it is part of package of project ${parentExt.packageOf}")
                        parentExt.name
                    } else {
                        ext.name
                    }
                publishing.repositories {
                    maven {
                        name = "bintray"
                        setUrl("https://api.bintray.com/maven/${bintray.subject}/${bintray.repo}/$pkg/;publish=0")
                        credentials {
                            username = bintray.username
                            password = bintray.apiKey
                        }
                    }
                }

                if (ext.packageOf == null) {
                    tasks.maybeCreate("create${pkg.capitalize()}PackageInBintrayRepository").apply {
                        group = "bintray"
                        onlyIf {
                            !bintray.dryRun && HttpClients.createDefault().use { client ->
                                client.execute(HttpGet("https://api.bintray.com/packages/${bintray.subject}/${bintray.repo}/$pkg")).use {
                                    it.statusLine.statusCode == 404
                                }
                            }
                        }
                        doLast {
                            val json = Gson().toJson(mapOf(
                                    "name" to ext.name,
                                    "desc" to ext.description,
                                    "licenses" to arrayOf("MIT"),
                                    "vcs_url" to "https://github.com/Kodein-Framework/${root.publication.repoName}.git",
                                    "website_url" to "http://kodein.org",
                                    "issue_tracker_url" to "https://github.com/Kodein-Framework/${root.publication.repoName}/issues"
                            ))
                            val request = Request.Builder()
                                    .url("https://api.bintray.com/packages/${bintray.subject}/${bintray.repo}")
                                    .post(json.toRequestBody("application/json".toMediaType()))
                                    .header("Authorization", Credentials.basic(bintray.username, bintray.apiKey))
                                    .build()
                            val response = OkHttpClient().newCall(request).execute()
                            check(response.isSuccessful) {
                                "Could not create package (HTTP status code ${response.code}): " + response.body?.string()
                            }
                        }
                    }
                }

                afterEvaluate {
                    tasks.withType<PublishToMavenRepository>()
                            .filter { it.repository.name == "bintray" }
                            .applyEach {
                                if (ext.packageOf != null) dependsOn("${ext.packageOf}:create${pkg.capitalize()}PackageInBintrayRepository")
                                else dependsOn("create${pkg.capitalize()}PackageInBintrayRepository")

                                onlyIf {
                                    logger.warn("${if (bintray.dryRun) "DRY RUN " else ""}Uploading '${publication.groupId}:${publication.artifactId}:${publication.version}' from publication '${publication.name}':")
                                    inputs.files.forEach {
                                        logger.warn("    - " + it.name)
                                    }
                                    !bintray.dryRun
                                }

                                doFirst {
                                    if (KodeinLocalPropertiesPlugin.on(project).getAsList("classpathFixes").isNotEmpty()) {
                                        error("Cannot publish to Bintray with classpath fixes!")
                                    }
                                    val excludeTargets = KodeinLocalPropertiesPlugin.on(project).getAsList("excludeTargets")
                                    if (excludeTargets.isNotEmpty()) {
                                        logger.warn("Uploading to Bintray with excluded targets $excludeTargets")
                                    }
                                }
                            }
                }

                if (ext.packageOf == null) {
                    val httpClient = OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build()

                    tasks.create("postBintrayPublish") {
                        group = "bintray"
                        onlyIf { !bintray.dryRun }
                        doLast {
                            val request = Request.Builder()
                                    .url("https://api.bintray.com/content/${bintray.subject}/${bintray.repo}/$pkg/${root.publication.version}/publish")
                                    .post("{}".toRequestBody("application/json".toMediaType()))
                                    .header("Authorization", Credentials.basic(bintray.username, bintray.apiKey))
                                    .build()
                            httpClient.newCall(request).execute()
                        }
                    }

                    tasks.create("postBintrayDiscard") {
                        group = "bintray"
                        onlyIf { !bintray.dryRun }
                        doLast {
                            val request = Request.Builder()
                                    .url("https://api.bintray.com/content/${bintray.subject}/${bintray.repo}/$pkg/${root.publication.version}/publish")
                                    .post("{ \"discard\": true }".toRequestBody("application/json".toMediaType()))
                                    .header("Authorization", Credentials.basic(bintray.username, bintray.apiKey))
                                    .build()
                            httpClient.newCall(request).execute()
                        }
                    }
                }
            }

            project.version = root.publication.version
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
                        url.set("https://github.com/Kodein-Framework/${root.publication.repoName}/issues")
                    }
                    scm {
                        connection.set("https://github.com/Kodein-Framework/${root.publication.repoName}.git")
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
        }

    }

}
