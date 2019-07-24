package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings
import org.gradle.api.internal.plugins.DslObject

class Framework(private val settings: Settings, framework: String) {

    companion object {
        private fun Settings.findStringProperty(property: String): String? =
                DslObject(this).asDynamicObject.tryGetProperty(property)
                        .let { if (it.isFound) (it.value as String) else null }
    }

    val isExcluded = (settings.findStringProperty("exclude${framework.capitalize()}") ?: System.getenv("EXCLUDE_${framework.toUpperCase()}")) == "true"
    val isIncluded get() = !isExcluded

    fun include(vararg projectPaths: String) {
        if (isIncluded) settings.include(*projectPaths)
    }
}
