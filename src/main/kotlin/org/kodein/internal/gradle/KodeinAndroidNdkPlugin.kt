package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File
import java.util.*

public class KodeinAndroidNdkPlugin : KtPlugin<Project> {

    public class Extension(private val project: Project) {

        public val version: Provider<String> = project.provider { project.kodeinGlobalVersion("android.ndk") }

        public val path: Provider<File> = project.provider {
            val androidHomePath = System.getenv("ANDROID_HOME")
                ?: run {
                    val localPropsFile = project.rootProject.file("local.properties")
                    check(localPropsFile.exists()) { "Please create android root local.properties" }
                    val localProps = localPropsFile.inputStream().use { Properties().apply { load(it) } }
                    localProps.getProperty("sdk.dir")
                        ?: error("Please set sdk.dir android sdk path in root local.properties")
                }
            val androidHome = File(androidHomePath)
            check(androidHome.exists()) { "${androidHome.absolutePath} does not exist" }
            val ndkDir = androidHome.resolve("ndk").resolve(version.get())
            check(ndkDir.exists()) { "Please install Android NDK $version" }
            ndkDir
        }
    }

    override fun Project.applyPlugin() {
        extensions.add("androidNdk", Extension(project))
    }
}
