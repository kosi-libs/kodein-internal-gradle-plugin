package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import org.jetbrains.kotlin.konan.target.Family

public class KodeinMppPlugin : KtPlugin<Project> {

    internal companion object {
        fun Project.applyMppPlugin(createExt: (Project, KotlinMultiplatformExtension) -> KodeinMppExtension) {
            apply {
                plugin("org.jetbrains.kotlin.multiplatform")
            }

            val kotlin = extensions.findByType<KotlinMultiplatformExtension>()!!

            val ext = createExt(this, kotlin)
            (kotlin as ExtensionAware).extensions.add("kodein", ext)

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            kotlin.applyDefaultHierarchyTemplate {
                common {
                    group("jvmBased") {
                        withJvm()
                        withAndroidTarget()
                    }
                    group("jsBased") {
                        withJs()
                        withWasmJs()
                        withWasmWasi()
                    }
                    group("wasm") {
                        withWasmJs()
                        withWasmWasi()
                    }
                    group("posix") {
                        withCompilations { it.target.let { target ->
                            target is KotlinNativeTarget && target.konanTarget.family != Family.MINGW }
                        }
                    }
                }
            }

            kotlin.sourceSets.getByName("commonTest").dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }

            kotlin.targets.configureEach {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            allWarningsAsErrors.set(provider { KodeinLocalPropertiesPlugin.on(project).isNotTrue("allowWarnings") })
                            freeCompilerArgs.add("-Xexpect-actual-classes")
                        }
                    }
                }
            }

            afterEvaluate {

                kotlin.sourceSets.all {
                    languageSettings.progressiveMode = true
                }

                val upload = project.plugins.findPlugin(KodeinUploadModulePlugin::class)

                val enableCrossCompilation = project.properties["org.kodein.native.enableCrossCompilation"] == "true"
                // https://youtrack.jetbrains.com/issue/KT-30498
                if (!enableCrossCompilation) {
                    ext.crossTargets
                        .mapNotNull { kotlin.targets.findByName(it) }
                        .forEach { target ->
                            target.compilations.configureEach {
                                compileTaskProvider.configure { enabled = false }
                            }

                            target.mavenPublication { upload?.disabledPublications?.add(this) }

                            if (this is KotlinNativeTarget) {
                                compilations.configureEach {
                                    cinterops.configureEach {
                                        tasks.named(interopProcessingTaskName).configure { enabled = false }
                                    }
                                    tasks.named(processResourcesTaskName).configure { enabled = false }
                                }
                                binaries.configureEach { linkTaskProvider.configure { enabled = false } }
                            }
                        }
                }

                if (upload != null) {
                    ext.hostTargets
                        .mapNotNull { kotlin.targets.findByName(it) }
                        .forEach { target ->
                            target.mavenPublication {
                                upload.hostOnlyPublications.add(this)
                            }
                        }
                }

                tasks.register("hostOnlyTest") {
                    group = "verification"
                    dependsOn(tasks.withType<KotlinTest>().filter { it.targetName in ext.hostTargets })
                }

            }

            configureTestLogsPrint()
        }
    }

    override fun Project.applyPlugin() {
        applyMppPlugin(::KodeinMppExtension)
    }

}
