package org.kodein.internal.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*

class KodeinMppPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin<KodeinVersionsPlugin>()
        }

        extensions.add("kodein", KodeinMPPExtension(this))

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
//                        TODO v3.0 move to 1.8 source compatibility
                        (kotlinOptions as? KotlinJvmOptions)?.jvmTarget = "1.6"
                    }
                }

                sourceSets.all {
                    languageSettings.progressiveMode = true
                }
            }
        }

        configureTestLogsPrint()
    }

}
