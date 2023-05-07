package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.plugin
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

public class KodeinGradlePluginPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinJvmPlugin>()
            plugin("java-gradle-plugin")
            plugin("maven-publish")
            plugin("com.gradle.plugin-publish")
            plugin<KodeinUploadModulePlugin>()
        }

        System.getenv("GRADLE_PUBLISH_KEY")?.let { extra["gradle.publish.key"] = it }
        System.getenv("GRADLE_PUBLISH_SECRET")?.let { extra["gradle.publish.secret"] = it }

        @Suppress("UnstableApiUsage")
        extensions.configure<GradlePluginDevelopmentExtension>("gradlePlugin") {
            website.set("http://kodein.org")
            vcsUrl.set("https://github.com/kosi-libs/${rootProject.name}.git")
        }

        extensions.configure<KodeinUploadModulePlugin.Extension>("kodeinUpload") {
            addJavadoc = false
            signPublications = false
        }
    }
}
