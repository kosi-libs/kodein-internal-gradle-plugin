package org.kodein.internal.gradle

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.plugin

class KodeinGradlePluginPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinJvmPlugin>()
            plugin("java-gradle-plugin")
            plugin("maven-publish")
            plugin("com.gradle.plugin-publish")
        }

        System.getenv("GRADLE_PUBLISH_KEY")?.let { extra["gradle.publish.key"] = it }
        System.getenv("GRADLE_PUBLISH_SECRET")?.let { extra["gradle.publish.secret"] = it }

        extensions.configure<PluginBundleExtension>("pluginBundle") {
            website = "http://kodein.org"
            vcsUrl = "https://github.com/Kodein-Framework/${rootProject.name}.git"
        }
    }
}
