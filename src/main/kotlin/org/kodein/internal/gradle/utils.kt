package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

internal interface KtPlugin<T : Project> : Plugin<T> {
    fun T.applyPlugin()
    override fun apply(target: T) = target.applyPlugin()
}

internal fun Project.configureTestLogsPrint() {
    afterEvaluate {
        tasks.withType<AbstractTestTask>().configureEach {
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}

internal fun Project.kodeinGlobalVersion(alias: String) =
    extensions.getByType<VersionCatalogsExtension>().named("kodeinGlobals")
        .findVersion(alias).orElseThrow { NoSuchElementException(alias) } .requiredVersion
