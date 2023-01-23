package org.kodein.internal.gradle

import org.gradle.api.*

@Suppress("unused")
public object KodeinVersions {

    public const val kotlin: String = BuildConfig.kotlinVersion
    public const val dokka: String = BuildConfig.dokkaVersion
    public const val androidBuildTools: String = BuildConfig.androidBuildToolsVersion
    public const val androidNdk: String = BuildConfig.androidNdkVersion

}

public class KodeinVersionsPlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinVersions", KodeinVersions)
    }

    override fun apply(project: Project){
        project.applyPlugin()
    }

}
