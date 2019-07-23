package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("UnstableApiUsage")
class KodeinLibraryMppWithAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin("com.android.library")
            plugin<KodeinLibraryMppPlugin>()
        }

        val android = extensions["android"] as LibraryExtension
        android.apply {
            sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }

        KodeinAndroidPlugin.configureAndroid(android)
    }

}
