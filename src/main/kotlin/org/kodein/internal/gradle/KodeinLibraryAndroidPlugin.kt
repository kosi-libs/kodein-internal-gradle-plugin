package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

public class KodeinLibraryAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinAndroidPlugin>()
            plugin("maven-publish")
            plugin<KodeinUploadModulePlugin>()
        }

        extensions.getByName<KotlinProjectExtension>("kotlin").explicitApi()

        afterEvaluate {
            project.extensions.getByName<PublishingExtension>("publishing")
                .publications.register<MavenPublication>(KodeinLibraryJvmPlugin.defaultPublicationName) {
                    from(components["release"])
                }
            KodeinLibraryJvmPlugin.addJvmSourcesJar(project) {
                extensions.getByName<LibraryExtension>("android").sourceSets["main"].java.srcDirs
            }
        }
    }

}
