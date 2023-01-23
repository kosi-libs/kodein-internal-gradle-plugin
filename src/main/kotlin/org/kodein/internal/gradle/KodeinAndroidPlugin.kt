package org.kodein.internal.gradle

import com.android.build.gradle.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*

@Suppress("UnstableApiUsage")
public class KodeinAndroidPlugin : KtPlugin<Project> {

    internal companion object {
        fun configureAndroid(project: Project, android: LibraryExtension) = with(android) {
            compileSdk = 32
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                minSdk = 21
                ndkVersion = project.kodeinGlobalVersion("androidNdk")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }

    override fun Project.applyPlugin() {
        apply {
            plugin("com.android.library")
            plugin("kotlin-platform-android")
        }

        configureAndroid(project, extensions["android"] as LibraryExtension)

        val kotlinVersion = kodeinGlobalVersion("kotlin")
        dependencies {
            "testImplementation"("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
            "testImplementation"("junit:junit:4.13.2")

            "androidTestImplementation"("androidx.test.ext:junit:1.1.4")
            "androidTestImplementation"("androidx.test.espresso:espresso-core:3.5.0")
        }

        afterEvaluate {
            extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets.all {
                languageSettings.progressiveMode = true
            }
        }

        KodeinJvmPlugin.configureJvmTarget(project)

        configureTestLogsPrint()
    }

}
