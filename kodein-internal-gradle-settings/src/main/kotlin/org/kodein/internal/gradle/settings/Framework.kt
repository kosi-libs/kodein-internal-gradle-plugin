package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings
import org.gradle.api.internal.plugins.DslObject

class Framework(private val settings: Settings, framework: String) {
    private fun isPropertyTrue(property: String) = DslObject(settings).asDynamicObject.tryGetProperty(property).let { it.isFound && it.value == "true" }

    val isExcluded = isPropertyTrue("exclude${framework.capitalize()}")
    val isIncluded get() = !isExcluded

    fun include(vararg projectPaths: String) {
        if (isIncluded) settings.include(*projectPaths)
    }
}
