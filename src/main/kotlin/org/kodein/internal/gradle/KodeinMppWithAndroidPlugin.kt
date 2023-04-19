package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

public class KodeinMppWithAndroidPlugin : KtPlugin<Project> {

    internal companion object {
        fun configureMPPAndroid(project: Project, excludeAndroid: Boolean) {
            if (!excludeAndroid) {
                project.apply { plugin("com.android.library") }
                val android = project.extensions["android"] as LibraryExtension
                KodeinAndroidPlugin.configureAndroid(project, android)
                project.extensions.add("kodeinAndroid", KodeinMppAndroidExtension(android))
            } else {
                project.extensions.add("kodeinAndroid", KodeinMppAndroidExtension(null))
            }
        }
    }

    override fun Project.applyPlugin() {
        with (KodeinMppPlugin) {
            applyMppPlugin(::KodeinMppWithAndroidExtension)
        }

        val kotlin = project.extensions.getByType<KotlinMultiplatformExtension>()
        val kodein = (kotlin as ExtensionAware).extensions.getByType<KodeinMppExtension>()
        val excludeAndroid = "android" in kodein.excludedTargets
        configureMPPAndroid(this, excludeAndroid)
    }

}
