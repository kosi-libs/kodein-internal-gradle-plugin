package org.kodein.internal.gradle

import org.gradle.api.*
import org.gradle.api.logging.LogLevel
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class KodeinMppPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin<KodeinVersionsPlugin>()
        }

        val ext = KodeinMPPExtension(this)
        extensions.add("kodein", ext)

        extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            sourceSets.apply {
                getByName("commonMain") {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                    }
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

                // https://youtrack.jetbrains.com/issue/KT-30498
                if (!ext.enableCrossCompilation) {
                    val upload = project.plugins.findPlugin(KodeinUploadPlugin::class)
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
            }

        }

        configureTestLogsPrint()
    }

}
