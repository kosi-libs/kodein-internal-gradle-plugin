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
            plugin<KodeinLibraryMppPlugin>()
            plugin("com.android.library")
        }

        KodeinAndroidPlugin.apply { configureAndroid() }

        extensions.configure<LibraryExtension>("android") {
            sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }

    }

}
