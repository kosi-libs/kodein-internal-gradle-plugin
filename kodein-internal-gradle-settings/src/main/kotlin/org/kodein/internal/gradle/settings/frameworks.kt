package org.kodein.internal.gradle.settings

import org.gradle.api.initialization.Settings

fun Settings.framework(name: String) = Framework(settings, name)

val Settings.android get() = framework("android")
