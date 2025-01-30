package org.kodein.internal.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public class KodeinJvmPlugin : KtPlugin<Project> {

    internal companion object {

        fun configureJvmTarget(project: Project) = with(project) {
            configureCommonJvmTarget(project)
            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    // In order to target a specific JDK to build onto
                    // https://jakewharton.com/kotlins-jdk-release-compatibility-flag/
                    // https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
                    freeCompilerArgs.add("-Xjdk-release=${jvmTarget(project).target}")
                }
            }
        }

        fun configureAndroidJvmTarget(project: Project) =
            configureCommonJvmTarget(project)

        private fun configureCommonJvmTarget(project: Project) = with(project) {
            tasks.withType<KotlinCompile>().configureEach {
                if (KodeinLocalPropertiesPlugin.on(project).isNotTrue("allowWarnings")) {
                    compilerOptions.allWarningsAsErrors.set(true)
                }
                compilerOptions {
                    val jvmTargetVersion = jvmTarget(project)
                    jvmTarget.set(jvmTargetVersion)
                }
            }

            extensions.getByType<JavaPluginExtension>().apply {
                sourceCompatibility = javaVersion(project)
                targetCompatibility = javaVersion(project)
            }

            tasks.register("jvmTest") {
                group = "verification"
                dependsOn("test")
            }
        }

        fun javaVersion(project: Project) =
            when (val version = project.properties["org.kodein.jvm-version"] ?: "11") {
                "11" -> JavaVersion.VERSION_11
                "17" -> JavaVersion.VERSION_17
                "21" -> JavaVersion.VERSION_21
                else -> error("Unsupported JVM version $version")
            }

        fun jvmTarget(project: Project) =
            when (val version = project.properties["org.kodein.jvm-version"] ?: "11") {
                "11" -> JvmTarget.JVM_11
                "17" -> JvmTarget.JVM_17
                "21" -> JvmTarget.JVM_21
                else -> error("Unsupported JVM version $version")
            }
    }

    override fun Project.applyPlugin() {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
        }

        dependencies {
            "testImplementation"("org.jetbrains.kotlin:kotlin-test")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
            "testImplementation"("junit:junit:4.13.2")
        }

        val kotlin = extensions.getByName<KotlinProjectExtension>("kotlin")
        afterEvaluate {
            kotlin.sourceSets.all {
                languageSettings.progressiveMode = true
            }

            configureJvmTarget(project)
        }

        configureTestLogsPrint()
    }

}
