package org.kodein.internal.gradle

import com.android.build.gradle.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*

@Suppress("UnstableApiUsage")
class KodeinAndroidPlugin : KtPlugin<Project> {

    companion object {
        internal fun configureAndroid(android: LibraryExtension) = with(android) {
            compileSdk = 32
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                minSdk = 21
                ndkVersion = KodeinVersions.androidNdk
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
            plugin<KodeinVersionsPlugin>()
        }

        configureAndroid(extensions["android"] as LibraryExtension)

        dependencies {
            "testImplementation"("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
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
