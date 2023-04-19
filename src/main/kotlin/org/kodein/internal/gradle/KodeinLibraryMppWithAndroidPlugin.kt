package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.plugin

public class KodeinLibraryMppWithAndroidPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinMppWithAndroidPlugin>()
        }

        with(KodeinLibraryMppPlugin) {
            applyLibrayMppPlugin()
        }
    }

}
