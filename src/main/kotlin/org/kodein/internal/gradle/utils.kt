package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
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
