package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.plugin

class KodeinLibraryAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinAndroidPlugin>()
            plugin("maven-publish")
//            plugin("java-library")
            plugin("digital.wup.android-maven-publish")
            plugin<KodeinUploadPlugin>()
        }

        extensions.add("kodeinLib", KodeinLibraryDependencyExtension(this))

        @Suppress("UnstableApiUsage")
        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("Kodein") {
                    from(components["android"])
                }
            }
        }
    }

}
