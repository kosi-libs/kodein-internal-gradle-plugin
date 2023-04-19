package org.gradle.kotlin.dsl

import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec
import org.kodein.internal.gradle.settings.KodeinPlugins


public fun PluginDependenciesSpec.kodein(module: String): PluginDependencySpec
    = id("org.kodein.$module")

public val PluginDependenciesSpec.kodein: KodeinPlugins get() = KodeinPlugins(this)
