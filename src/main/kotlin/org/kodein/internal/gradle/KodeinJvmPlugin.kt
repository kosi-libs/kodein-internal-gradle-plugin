package org.kodein.internal.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("UnstableApiUsage")
class KodeinJvmPlugin : KtPlugin<Project> {

    companion object {

        internal fun configureJvm18(project: Project) = with(project) {
            tasks.withType<KotlinCompile>().all {
                sourceCompatibility = "1.8"
                targetCompatibility = "1.8"
                kotlinOptions.jvmTarget = "1.8"
            }

            project.withConvention(JavaPluginConvention::class) {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }

    }

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

            configureJvm18(this)
        }

        configureTestLogsPrint()
    }

}
