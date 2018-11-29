package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

@Suppress("UnstableApiUsage")
class KodeinLibraryJvmPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinJvmPlugin>()
            plugin("maven-publish")
            plugin("java-library")
            plugin<KodeinUploadPlugin>()
        }

        extensions.add("kodeinLib", KodeinLibraryDependencyExtension(this))

        DependencyHandlerScope(dependencies).apply {
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk7")

            "testImplementation"("org.jetbrains.kotlin:kotlin-test")
            "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
            "testImplementation"("junit:junit:4.12")
        }

        val sourcesJar = task<Jar>("sourcesJar") {
            classifier = "sources"
            setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE)
        }

        afterEvaluate {
            val sourceSets = project.convention.getPluginByName<org.gradle.api.plugins.JavaPluginConvention>("java").sourceSets
            sourcesJar.from(sourceSets["main"].allSource)

            extensions.configure<PublishingExtension>("publishing") {
                (publications) {
                    "Kodein"(MavenPublication::class) {
                        from(components["java"])
                        artifact(sourcesJar)
                    }
                }
            }
        }

        printTestLogs()
    }

}
