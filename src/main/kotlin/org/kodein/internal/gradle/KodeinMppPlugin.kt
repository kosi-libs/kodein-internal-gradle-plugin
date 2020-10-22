package org.kodein.internal.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

class KodeinMppPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin<KodeinVersionsPlugin>()
        }

        val ext = KodeinMppExtension(this)
        extensions.add("kodein", ext)

        extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            sourceSets.apply {
                getByName("commonMain") {
                    languageSettings.progressiveMode = true
                }
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
                        (kotlinOptions as? KotlinJvmOptions)?.jvmTarget = "1.8"
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
                            .applyEach {
                                compilations.applyEach {
                                    compileKotlinTask.enabled = false
                                }

                                mavenPublication {
                                    upload?.disabledPublications?.add(this)
                                }

                                if (this is KotlinNativeTarget) {
                                    compilations.applyEach {
                                        cinterops.applyEach {
                                            tasks[interopProcessingTaskName].enabled = false
                                        }
                                        tasks[this.processResourcesTaskName].enabled = false
                                    }
                                    binaries.applyEach {
                                        linkTask.enabled = false
                                    }
                                }
                            }
                }

                if (upload != null) {
                    ext.hostTargets
                            .mapNotNull { targets.findByName(it) }
                            .applyEach {
                                mavenPublication {
                                    upload.hostOnlyPublications.add(this)
                                }
                            }
                }

                tasks.create("hostOnlyTest") {
                    group = "verification"
                    tasks.withType<KotlinTest>()
                            .filter { it.targetName in ext.hostTargets }
                            .forEach {
                                dependsOn(it)
                            }
                }

            }

        }

        configureTestLogsPrint()
    }

}
