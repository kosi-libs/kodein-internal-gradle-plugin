package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KodeinMppWithAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        val excludeAndroid = isPropertyTrue("excludeAndroid")

        apply {
            if (!excludeAndroid) plugin("com.android.library")
            plugin<KodeinMppPlugin>()
        }

        if (!excludeAndroid) {
            val android = extensions["android"] as LibraryExtension
            KodeinAndroidPlugin.configureAndroid(android)
            extensions.add("kodeinAndroid", KodeinMppAndroidExtension(android))
        } else {
            extensions.add("kodeinAndroid", KodeinMppAndroidExtension(null))
        }
    }

}
