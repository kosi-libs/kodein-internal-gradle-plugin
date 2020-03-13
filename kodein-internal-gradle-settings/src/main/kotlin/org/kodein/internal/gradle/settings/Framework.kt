package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings
import org.gradle.api.internal.plugins.DslObject

class Framework(private val settings: Settings, framework: String) {

    companion object {
        private fun Settings.findStringProperty(property: String): String? =
                DslObject(this).asDynamicObject.tryGetProperty(property)
                        .let { if (it.isFound) (it.value as String) else null }
    }

    val excludedTargets = settings.findStringProperty("excludeTargets")
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

    val isExcluded =
            (framework in excludedTargets)
            || (System.getenv("EXCLUDE_${framework.toUpperCase()}") != null)

    val isIncluded get() = !isExcluded

    fun include(vararg projectPaths: String) {
        if (isIncluded) settings.include(*projectPaths)
    }
}
