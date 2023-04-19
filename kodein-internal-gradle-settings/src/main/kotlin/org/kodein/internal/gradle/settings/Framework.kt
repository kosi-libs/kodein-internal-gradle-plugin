package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings
import java.util.*

public class Framework(private val settings: Settings, framework: String) {

    public val excludedTargets: List<String> = KodeinSettingsPlugin.get(settings).findLocalProperty("excludeTargets")
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

    public val isExcluded: Boolean =
            (framework in excludedTargets)
            || (System.getenv("EXCLUDE_${framework.uppercase(Locale.getDefault())}") != null)

    public val isIncluded: Boolean get() = !isExcluded

    public fun include(vararg projectPaths: String) {
        if (isIncluded) settings.include(*projectPaths)
    }
}
