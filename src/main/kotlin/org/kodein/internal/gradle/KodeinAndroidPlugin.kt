package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

@Suppress("UnstableApiUsage")
class KodeinAndroidPlugin : KtPlugin<Project> {

    companion object {
        fun Project.configureAndroid() {
            extensions.configure<LibraryExtension>("android") {
                compileSdkVersion(28)
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    minSdkVersion(21)
                }
            }
        }
    }

    override fun Project.applyPlugin() {
        apply {
            plugin("com.android.library")
            plugin("kotlin-platform-android")
            plugin<KodeinVersionsPlugin>()
        }

        configureAndroid()

        dependencies {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
            "testImplementation"("junit:junit:4.12")
        }

        afterEvaluate {
            extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets.forEach { it.languageSettings.progressiveMode = true }
        }

        printTestLogs()
    }

}
