package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.register

public class KodeinRootPlugin : KtPlugin<Project> {

    public inner class Extension(private val project: Project) {
        public fun experimentalCompose(version: Provider<String>) {
            // https://github.com/Kotlin/kotlin-wasm-examples/blob/main/compose-imageviewer/build.gradle.kts
            project.allprojects {
                configurations.all {
                    val conf = this
                    // Currently it's necessary to make the android build work properly
                    conf.resolutionStrategy.eachDependency {
                        val isWasm = conf.name.contains("wasm", true)
                        val isJs = conf.name.contains("js", true)
                        val isComposeGroup = requested.module.group.startsWith("org.jetbrains.compose")
                        val isComposeCompiler = requested.module.group.startsWith("org.jetbrains.compose.compiler")
                        if (isComposeGroup && !isComposeCompiler && !isWasm && !isJs) {
                            val stableVersion = (
                                    Regex("(.+)-dev-wasm[0-9]+").matchEntire(version.get())
                                        ?: error("Compose version \"${version.get()}\" does not match \"(.+)-dev-wasm[0-9]+\" (for example: \"1.4.0-dev-wasm06\").")
                                    ).groupValues[1]
                            useVersion(stableVersion)
                        }
                    }
                }
            }

        }
    }

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinUploadRootPlugin>()
        }

        allprojects {
            tasks.register<DependencyReportTask>("allDependencies")
        }

        extensions.add("kodein", Extension(this))
    }

}
