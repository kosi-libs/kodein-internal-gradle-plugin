package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings

public fun Settings.framework(name: String): Framework = Framework(settings, name)

public val Settings.android: Framework get() = framework("android")
