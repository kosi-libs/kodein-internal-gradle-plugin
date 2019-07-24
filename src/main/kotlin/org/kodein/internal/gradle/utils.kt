package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.kotlin.dsl.withType

internal interface KtPlugin<T> : Plugin<T> {
    fun T.applyPlugin()
    override fun apply(target: T) = target.applyPlugin()
}

internal fun Project.configureTestLogsPrint() {
    afterEvaluate {
        tasks.withType<AbstractTestTask>().forEach {
            it.testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}

fun Project.isTrue(property: String) = findProperty(property) == "true"

fun Project.isExcluded(framework: String) =
        (findProperty("exclude${framework.capitalize()}") ?: System.getenv("EXCLUDE_${framework.toUpperCase()}")) == "true"
