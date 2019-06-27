package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kotlin.js.jstests.node.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*

class KodeinMppPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.multiplatform")
            plugin<KotlinMppJsTestsNodePlugin>()
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
                jvm {
                    compilations.getting {
                        kotlinOptions {
                            jvmTarget = "1.8"
                        }
                    }
                }
                sourceSets.forEach {
                    it.languageSettings.progressiveMode = true
                }
            }
        }

        printTestLogs()
    }

}
