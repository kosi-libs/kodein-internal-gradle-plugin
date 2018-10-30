package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope

class KodeinAndroidPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("com.android.library")
            plugin("kotlin-platform-android")
        }

        extensions.configure<LibraryExtension>("android") {
            compileSdkVersion(28)

            defaultConfig {
                minSdkVersion(15)
            }

            dexOptions {
                val travisBuild = System.getenv("TRAVIS") == "true"
                val preDexEnabled = System.getProperty("pre-dex", "true") == "true"
                preDexLibraries = preDexEnabled && !travisBuild
            }
        }

        DependencyHandlerScope(dependencies).apply {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
            "testImplementation"("junit:junit:4.12")
        }

        printTestLogs()
    }

    override fun apply(project: Project) = project.applyPlugin()

}
