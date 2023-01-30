package org.kodein.internal.gradle

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("UnstableApiUsage")
public class KodeinJvmPlugin : KtPlugin<Project> {

    internal companion object {

        fun configureJvmTarget(project: Project) = with(project) {
            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions.jvmTarget.set(jvmTarget(project))
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
            when (val version = project.properties["org.kodein.jvm-version"] ?: "1.8") {
                "1.8" -> JavaVersion.VERSION_1_8
                "11" -> JavaVersion.VERSION_11
                "17" -> JavaVersion.VERSION_17
                else -> error("Unsupported JVM version $version")
            }

        fun jvmTarget(project: Project) =
            when (val version = project.properties["org.kodein.jvm-version"] ?: "1.8") {
                "1.8" -> JvmTarget.JVM_1_8
                "11" -> JvmTarget.JVM_11
                "17" -> JvmTarget.JVM_17
                else -> error("Unsupported JVM version $version")
            }

    }

    override fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-jvm")
        }

        dependencies {
            "testImplementation"("org.jetbrains.kotlin:kotlin-test")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
            "testImplementation"("junit:junit:4.13.2")
        }

        afterEvaluate {
            extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets.all {
                languageSettings.progressiveMode = true
            }

            configureJvmTarget(project)
        }

        configureTestLogsPrint()
    }

}
