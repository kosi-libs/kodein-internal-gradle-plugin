package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin

@Suppress("UnstableApiUsage")
class KodeinLibraryMppWithAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        val excludedTargets = KodeinLocalPropertiesPlugin.on(this).getAsList("excludeTargets")

        apply { plugin<KodeinLibraryMppPlugin>() }

        val excludeAndroid = "android" in excludedTargets || KodeinMppExtension.Targets.jvm.android in project.extensions.getByName<KodeinMppExtension>("kodein").excludedTargets

        KodeinMppWithAndroidPlugin.configureMPPAndroid(this, excludeAndroid)
    }

}
