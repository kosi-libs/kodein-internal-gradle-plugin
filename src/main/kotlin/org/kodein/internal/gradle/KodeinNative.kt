package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

object KodeinNative {
    val targets = listOf(
            "androidNativeArm32", "androidNativeArm64",
            "iosArm32", "iosArm64", "iosX64",
            "linuxArm32Hfp", "linuxMips32", "linuxMipsel32", "linuxX64",
            "macosX64",
            "mingwX64"
    )

    val sourceSets = targets.map { it + "Main" }
}

class KodeinNativePlugin : Plugin<Project> {

    private fun Project.applyPlugin() {
        extensions.add("kodeinNative", KodeinNative)
    }

    override fun apply(target: Project) = target.applyPlugin()

}