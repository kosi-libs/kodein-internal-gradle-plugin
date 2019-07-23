package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.plugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KodeinMppWithAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppPlugin>()
            plugin("com.android.library")
        }

        KodeinAndroidPlugin.configureAndroid(this)
    }

}
