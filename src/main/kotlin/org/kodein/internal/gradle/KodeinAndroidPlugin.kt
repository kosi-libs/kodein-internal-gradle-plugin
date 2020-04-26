package org.kodein.internal.gradle

import com.android.build.gradle.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*

@Suppress("UnstableApiUsage")
class KodeinAndroidPlugin : KtPlugin<Project> {

    companion object {
        internal fun configureAndroid(android: LibraryExtension) = with(android) {
            compileSdkVersion(28)
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                minSdkVersion(16)
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            packagingOptions {
                exclude("META-INF/*.kotlin_module")
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
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KodeinVersions.kotlin}")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
            "testImplementation"("junit:junit:4.12")

            "androidTestImplementation"("androidx.test.ext:junit:1.1.1")
            "androidTestImplementation"("androidx.test.espresso:espresso-core:3.2.0")

        }

        afterEvaluate {
            extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets.forEach { it.languageSettings.progressiveMode = true }
        }

        KodeinJvmPlugin.configureJvmTarget(this)

        configureTestLogsPrint()
    }

}
