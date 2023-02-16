package org.kodein.internal.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

public class KodeinMppPlugin : KtPlugin<Project> {
    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
        }

        val ext = KodeinMppExtension(this)
        extensions.add("kodein", ext)

        extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            sourceSets.apply {
                getByName("commonTest") {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-test-common")
                        implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                    }
                }
            }

            afterEvaluate {
                targets.all {
                    compilations.all {
                        compilerOptions.configure {
                            if (KodeinLocalPropertiesPlugin.on(project).isNotTrue("allowWarnings")) {
                                allWarningsAsErrors.set(true)
                            }
                        }
                    }
                }

                sourceSets.all {
                    languageSettings.progressiveMode = true
                }

                val upload = project.plugins.findPlugin(KodeinUploadModulePlugin::class)

                val enableCrossCompilation = project.properties["org.kodein.native.enableCrossCompilation"] == "true"
                // https://youtrack.jetbrains.com/issue/KT-30498
                if (!enableCrossCompilation) {
                    ext.crossTargets
                            .mapNotNull { targets.findByName(it) }
                            .forEach { target ->
                                target.compilations.configureEach {
                                    compileTaskProvider.configure {
                                        enabled = false
                                    }
                                }

                                target.mavenPublication {
                                    upload?.disabledPublications?.add(this)
                                }

                                if (this is KotlinNativeTarget) {
                                    compilations.configureEach {
                                        cinterops.configureEach {
                                            tasks[interopProcessingTaskName].enabled = false
                                        }
                                        tasks[this.processResourcesTaskName].enabled = false
                                    }
                                    binaries.configureEach {
                                        linkTask.enabled = false
                                    }
                                }
                            }
                }

                if (upload != null) {
                    ext.hostTargets
                            .mapNotNull { targets.findByName(it) }
                            .forEach { target ->
                                target.mavenPublication {
                                    upload.hostOnlyPublications.add(this)
                                }
                            }
                }

                tasks.register("hostOnlyTest") {
                    val hostOnlyTest = this
                    group = "verification"
                    tasks.withType<KotlinTest>().all {
                        if (this.targetName in ext.hostTargets) {
                            hostOnlyTest.dependsOn(this)
                        }
                    }
                }

            }

        }

        configureTestLogsPrint()
    }

}
