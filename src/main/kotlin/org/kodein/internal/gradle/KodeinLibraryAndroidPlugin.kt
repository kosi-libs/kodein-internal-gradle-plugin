package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.task

@Suppress("UnstableApiUsage")
class KodeinLibraryAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinAndroidPlugin>()
            plugin("maven-publish")
            plugin("digital.wup.android-maven-publish")
            plugin<KodeinUploadPlugin>()
        }

        val ext = KodeinLibraryDependencyExtension(this)
        extensions.add("kodeinLib", ext)

        val sourcesJar = task<Jar>("sourcesJar") {
            classifier = "sources"
            setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
        }

        @Suppress("UnstableApiUsage")
        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("Kodein") {
                    from(components["android"])
                    artifact(sourcesJar)

                    ext.updatPom(this)
                }
            }
        }
    }

}
