package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.plugin

public class KodeinMppWithAndroidPlugin : KtPlugin<Project> {

    internal companion object {
        fun configureMPPAndroid(project: Project, excludeAndroid: Boolean) {
            if (!excludeAndroid) {
                project.apply { plugin("com.android.library") }
                val android = project.extensions["android"] as LibraryExtension
                android.apply {
                    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
                }
                KodeinAndroidPlugin.configureAndroid(project, android)
                project.extensions.add("kodeinAndroid", KodeinMppAndroidExtension(android))
            } else {
                project.extensions.add("kodeinAndroid", KodeinMppAndroidExtension(null))
            }

        }
    }

    override fun Project.applyPlugin() {
        val excludedTargets = KodeinLocalPropertiesPlugin.on(this).getAsList("excludeTargets")
        val excludeAndroid = "android" in excludedTargets

        apply { plugin<KodeinMppPlugin>() }

        configureMPPAndroid(this, excludeAndroid)
    }

}
