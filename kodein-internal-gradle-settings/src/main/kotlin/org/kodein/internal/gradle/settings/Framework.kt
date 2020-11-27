package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings

class Framework(private val settings: Settings, framework: String) {

    val excludedTargets = KodeinSettingsPlugin.get(settings).findLocalProperty("excludeTargets")
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
