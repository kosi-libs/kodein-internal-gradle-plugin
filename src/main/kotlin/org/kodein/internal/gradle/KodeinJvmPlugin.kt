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
//            TODO v3.0 move to 1.8 source compatibility
            tasks.withType<KotlinCompile>().all {
                sourceCompatibility = "1.6"
                targetCompatibility = "1.6"
                kotlinOptions.jvmTarget = "1.6"
            }

            project.withConvention(JavaPluginConvention::class) {
                sourceCompatibility = JavaVersion.VERSION_1_6
                targetCompatibility = JavaVersion.VERSION_1_6
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

            configureJvmTarget(this)
        }

        configureTestLogsPrint()
    }

}
