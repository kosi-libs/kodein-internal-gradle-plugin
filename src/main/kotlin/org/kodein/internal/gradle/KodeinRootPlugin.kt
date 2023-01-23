package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories

public class KodeinRootPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        apply {
            plugin<KodeinUploadRootPlugin>()
        }

        allprojects {
            repositories {
                mavenLocal()
                mavenCentral()
                google()
                maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
                maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
                maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
            }

            tasks.register<DependencyReportTask>("allDependencies")
        }
    }

}
