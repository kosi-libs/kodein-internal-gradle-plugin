package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.io.File
import java.util.*


class KodeinJvmExtension(val project: Project) {

    fun DependencyHandler.compileOnlyAndroidJar(version: Int = 19, laterVersionAllowed: Boolean = true) {
        add("compileOnly", androidJar(project, version, laterVersionAllowed))
    }

    companion object {
        internal fun androidJar(project: Project, requiredVersion: Int = 19, laterVersionAllowed: Boolean = true): Any {
            val file = project.rootDir.resolve("local.properties")
            if (!file.exists()) error("Please add local.properties root file with sdk.dir pointing to Android SDK directory")
            val properties = file.inputStream().use { Properties().apply { load(it) } }
            val sdkDirProperty = properties.getProperty("sdk.dir") ?: error("No sdk.dir in local.properties")
            val platformsDir = File(sdkDirProperty).resolve("platforms").takeIf { it.exists() } ?: error("$sdkDirProperty does not exist")
            val actualVersion =
                    if (laterVersionAllowed) {
                        platformsDir.list()!!
                                .filter { it.startsWith("android-") }
                                .map { it.substring(8).toInt() }
                                .max()
                                ?.takeIf { it >= requiredVersion }
                                ?: error("Please install an Android SDK greater of equal to $requiredVersion")
                    }
                    else {
                        requiredVersion.takeIf { platformsDir.resolve("android-$requiredVersion").exists() }
                                ?: error("Please install Android SDK version $requiredVersion")
                    }

            return project.files(platformsDir.resolve("android-$actualVersion").resolve("android.jar"))
        }
    }
}
