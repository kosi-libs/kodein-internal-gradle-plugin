package org.kodein.internal.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

public class KodeinAndroidPlugin : KtPlugin<Project> {

    internal companion object {
        fun configurePureAndroidLibrary(project: Project, android: LibraryExtension) = with(android) {
            publishing {
                singleVariant("release") {}
            }
            compileSdk = 36
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                minSdk = 21
                ndkVersion = project.kodeinGlobalVersion("android-ndk")
            }
            compileOptions {
                val version = KodeinJvmPlugin.javaVersion(project)
                sourceCompatibility = version
                targetCompatibility = version
            }
        }
    }

    override fun Project.applyPlugin() {
        apply {
            plugin("com.android.library")
            plugin<KodeinAndroidNdkPlugin>()
        }

        configurePureAndroidLibrary(project, extensions["android"] as LibraryExtension)

        val kotlinVersion = kodeinGlobalVersion("kotlin")
        dependencies {
            "testImplementation"("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
            "testImplementation"("junit:junit:4.13.2")

            "androidTestImplementation"("androidx.test.ext:junit:1.2.1")
            "androidTestImplementation"("androidx.test.espresso:espresso-core:3.6.1")
        }

        val kotlin = extensions.getByName<KotlinProjectExtension>("kotlin")
        afterEvaluate {
            kotlin.sourceSets.all {
                languageSettings.progressiveMode = true
            }
        }

        KodeinJvmPlugin.configureAndroidJvmTarget(project)

        configureTestLogsPrint()
    }

}
