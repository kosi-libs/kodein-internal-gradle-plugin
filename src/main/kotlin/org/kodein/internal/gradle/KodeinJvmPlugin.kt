package org.kodein.internal.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public class KodeinJvmPlugin : KtPlugin<Project> {

    internal companion object {

        fun configureJvmTarget(project: Project, kotlin: KotlinBaseExtension) = with(project) {
            kotlin.jvmToolchain(jvmTarget(project))
            tasks.withType<KotlinCompile>().configureEach {
                if (KodeinLocalPropertiesPlugin.on(project).isNotTrue("allowWarnings")) {
                    compilerOptions.allWarningsAsErrors.set(true)
                }
            }

            extensions.getByType<JavaPluginExtension>().apply {
                val version = javaVersion(project)
                sourceCompatibility = version
                targetCompatibility = version
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
                "11" -> 11
                "17" -> 17
                "21" -> 21
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

            configureJvmTarget(project, kotlin)
        }

        configureTestLogsPrint()
    }

}
