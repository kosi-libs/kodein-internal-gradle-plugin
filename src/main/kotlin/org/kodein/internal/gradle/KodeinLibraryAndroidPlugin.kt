package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

class KodeinLibraryAndroidPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("com.android.library")
            plugin("kotlin-platform-android")
            plugin("org.gradle.maven-publish")
            plugin("digital.wup.android-maven-publish")
//            plugin<KodeinDokka>()
//            plugin<KodeinKotlinPublish>()
//            plugin<KodeinPublicationUpload>()
            plugin<KodeinVersionsPlugin>()
        }

        extensions.configure<LibraryExtension>("android") {
            compileSdkVersion(27)

            defaultConfig {
                minSdkVersion(15)
            }

            dexOptions {
                val travisBuild = System.getenv("TRAVIS") == "true"
                val preDexEnabled = System.getProperty("pre-dex", "true") == "true"
                preDexLibraries = preDexEnabled && !travisBuild
            }
        }

        afterEvaluate {
            tasks.withType<Test>().forEach {
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                }
            }
        }

        DependencyHandlerScope(dependencies).apply {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
            "testImplementation"("junit:junit:4.12")
        }

        @Suppress("UnstableApiUsage")
        extensions.configure<PublishingExtension>("publishing") {
            (publications) {
                "Kodein"(MavenPublication::class) {
                    from(components["android"])
                }
            }
        }

    }

    override fun apply(project: Project) = project.applyPlugin()

}
