package org.kodein.internal.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

public class KodeinMppWithAndroidPlugin : KtPlugin<Project> {

    internal companion object {
        fun configureMPPAndroid(project: Project, excludeAndroid: Boolean) {
            if (excludeAndroid) return

            project.apply { plugin("com.android.kotlin.multiplatform.library") }

            val kotlin = project.extensions.getByType<KotlinMultiplatformExtension>()
            val androidTarget = kotlin.targets.getByName("android") as KotlinMultiplatformAndroidLibraryTarget

            configureKmpAndroidLibrary(androidTarget)
        }

        private fun configureKmpAndroidLibrary(android: KotlinMultiplatformAndroidLibraryTarget) {
            android.compileSdk = 36
            android.minSdk = 21
            android.withHostTest {}
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
