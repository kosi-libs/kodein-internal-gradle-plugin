package org.kodein.internal.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KodeinJvmPlugin : KtPlugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-jvm")
            plugin("org.gradle.maven-publish")
            plugin<KodeinVersionsPlugin>()
        }

        dependencies {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
            "testImplementation"("junit:junit:4.12")
        }

        afterEvaluate {
            extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets.forEach {
                it.languageSettings.progressiveMode = true
            }

            tasks.withType<KotlinCompile>().all {
                sourceCompatibility = "1.8"
                targetCompatibility = "1.8"
                kotlinOptions.jvmTarget = "1.8"
            }

            val javaPlugin = project.convention.getPluginByName<org.gradle.api.plugins.JavaPluginConvention>("java")
            javaPlugin.sourceCompatibility = JavaVersion.VERSION_1_8
            javaPlugin.targetCompatibility = JavaVersion.VERSION_1_8
        }

        printTestLogs()
    }

}
