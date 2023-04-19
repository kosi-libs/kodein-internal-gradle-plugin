package org.kodein.internal.gradle.settings

import org.gradle.kotlin.dsl.kodein
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

public class KodeinPlugins internal constructor(private val plugins: PluginDependenciesSpec) {

    public val android: PluginDependencySpec get() = plugins.kodein("android")

    public val androidNdk: PluginDependencySpec get() = plugins.kodein("android-ndk")

    public val gradlePlugin: PluginDependencySpec get() = plugins.kodein("gradle-plugin")

    public val jvm: PluginDependencySpec get() = plugins.kodein("jvm")

    public val localProperties: PluginDependencySpec get() = plugins.kodein("local-properties")

    public val mpp: PluginDependencySpec get() = plugins.kodein("mpp")

    public val mppWithAndroid: PluginDependencySpec get() = plugins.kodein("mpp-with-android")

    public val root: PluginDependencySpec get() = plugins.kodein("root")

    public val upload: PluginDependencySpec get() = plugins.kodein("upload")

    public val uploadRoot: PluginDependencySpec get() = plugins.kodein("upload-root")

    public inner class Library {

        public val android: PluginDependencySpec get() = plugins.kodein("library.android")

        public val jvm: PluginDependencySpec get() = plugins.kodein("library.jvm")

        public val mpp: PluginDependencySpec get() = plugins.kodein("library.mpp")

        public val mppWithAndroid: PluginDependencySpec get() = plugins.kodein("library.mpp-with-android")
    }

    public val library: Library get() = Library()
}
