package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin

@Suppress("UnstableApiUsage")
class KodeinLibraryMppWithAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        val excludedTargets = KodeinLocalPropertiesPlugin.on(this).getAsList("excludeTargets")

        apply { plugin<KodeinLibraryMppPlugin>() }

        val androidExcluded = "android" in excludedTargets || KodeinMppExtension.Targets.jvm.android in project.extensions.getByName<KodeinMppExtension>("kodein").excludedTargets

        if (!androidExcluded) apply { plugin("com.android.library") }

        if (!androidExcluded) {
            val android = extensions["android"] as LibraryExtension
            android.apply {
                sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
            }
            KodeinAndroidPlugin.configureAndroid(android)
            extensions.add("kodeinAndroid", KodeinMppAndroidExtension(android))
        } else {
            extensions.add("kodeinAndroid", KodeinMppAndroidExtension(null))
        }
    }

}
