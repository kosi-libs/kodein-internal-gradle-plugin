package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

@Suppress("UnstableApiUsage")
class KodeinLibraryAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinAndroidPlugin>()
            plugin("maven-publish")
            plugin("digital.wup.android-maven-publish")
            plugin<KodeinUploadPlugin>()
        }

        extensions.getByName<KotlinProjectExtension>("kotlin").explicitApi()

        val ext = KodeinLibraryDependencyExtension(this)
        extensions.add("kodeinLib", ext)
        
        val android = this@applyPlugin.extensions.getByName("android") as LibraryExtension

        val sourcesJar = task<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(android.sourceSets["main"].java.srcDirs)
        }

        @Suppress("UnstableApiUsage")
        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("Kodein") {
                    from(components["android"])
                    artifact(sourcesJar)

                    ext.updatePom(this)
                }
            }
        }
    }

}
