package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

internal interface KtPlugin<T> : Plugin<T> {
    fun T.applyPlugin()
    override fun apply(target: T) = target.applyPlugin()
}


fun Project.printTestLogs() {
    afterEvaluate {
        tasks.withType<Test>().forEach {
            it.testLogging {
                events("passed", "skipped", "failed", "standardOut", "standardError")
            }
        }
    }
}
