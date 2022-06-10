package org.kodein.internal.gradle

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("UnstableApiUsage")
class KodeinJvmPlugin : KtPlugin<Project> {

    companion object {

        internal fun configureJvmTarget(project: Project) = with(project) {
            tasks.withType<KotlinCompile>().all {
                kotlinOptions.jvmTarget = "1.8"
                kotlinOptions.allWarningsAsErrors = true
            }

            with(extensions.getByType<JavaPluginExtension>()) {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            tasks.create("jvmTest") {
                group = "verification"
                dependsOn("test")
            }
        }

    }

    override fun Project.applyPlugin() {
        apply {
            plugin("kotlin-platform-jvm")
            plugin<KodeinVersionsPlugin>()
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
