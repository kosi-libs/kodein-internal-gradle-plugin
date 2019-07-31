package org.kodein.internal.gradle.task

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.testing.TestTaskReports
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.DefaultProcessForkOptions
import org.gradle.testing.base.plugins.TestingBasePlugin
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesClientSettings
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.targets.native.internal.parseKotlinNativeStackTraceAsJvm
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import java.io.File

open class KotlinIosTest : KotlinTest() {

    @Suppress("LeakingThis")
    private val processOptions: ProcessForkOptions = DefaultProcessForkOptions(fileResolver)

    @InputFile
    @SkipWhenEmpty
    val executableProperty: Property<File> = project.objects.property(File::class.java)

    @Input
    var args: List<String> = emptyList()

    @Input
    var device: String = "iPhone 8"

    // Already taken into account in the executableProperty.
    @get:Internal
    var executable: File
        get() = executableProperty.get()
        set(value) {
            executableProperty.set(value)
        }

    @get:Input
    var workingDir: String
        get() = processOptions.workingDir.canonicalPath
        set(value) {
            processOptions.workingDir = File(value)
        }

    @get:Input
    var environment: Map<String, Any>
        get() = processOptions.environment
        set(value) {
            processOptions.environment = value
        }

    private fun <T> Property<T>.set(providerLambda: () -> T) = set(project.provider { providerLambda() })

    fun executable(file: File) {
        executableProperty.set(file)
    }

    fun executable(path: String) {
        executableProperty.set { project.file(path) }
    }

    fun executable(provider: () -> File) {
        executableProperty.set(provider)
    }

    fun executable(provider: Closure<File>) {
        executableProperty.set(project.provider(provider))
    }

    fun environment(name: String, value: Any) {
        processOptions.environment(name, value)
    }

    override fun createTestExecutionSpec(): TCServiceMessagesTestExecutionSpec {
        val extendedForkOptions = DefaultProcessForkOptions(fileResolver)
        processOptions.copyTo(extendedForkOptions)
        extendedForkOptions.executable = "xcrun"

        val clientSettings = TCServiceMessagesClientSettings(
                name,
                testNameSuffix = targetName,
                prependSuiteName = targetName != null,
                treatFailedTestOutputAsStacktrace = false,
                stackTraceParser = ::parseKotlinNativeStackTraceAsJvm
        )

        val cliArgs = CliArgs("TEAMCITY", includePatterns, excludePatterns, args)

        return TCServiceMessagesTestExecutionSpec(
                extendedForkOptions,
                listOf( "simctl", "spawn", device, executable.absolutePath) + cliArgs.toList(),
                false,
                clientSettings
        )
    }

    private class CliArgs(
            val testLogger: String? = null,
            val testGradleFilter: Set<String> = setOf(),
            val testNegativeGradleFilter: Set<String> = setOf(),
            val userArgs: List<String> = emptyList()
    ) {
        fun toList() = mutableListOf<String>().also {
            if (testLogger != null) {
                it.add("--ktest_logger=$testLogger")
            }

            if (testGradleFilter.isNotEmpty()) {
                it.add("--ktest_gradle_filter=${testGradleFilter.joinToString(",")}")
            }

            if (testNegativeGradleFilter.isNotEmpty()) {
                it.add("--ktest_negative_gradle_filter=${testNegativeGradleFilter.joinToString(",")}")
            }

            it.addAll(userArgs)
        }
    }

}

internal val Project.testResultsDir: File
    get() = project.buildDir.resolve(TestingBasePlugin.TEST_RESULTS_DIR_NAME)
internal val Project.reportsDir: File
    get() = project.extensions.getByType(ReportingExtension::class.java).baseDir
@Suppress("UnstableApiUsage")
internal val Project.testReportsDir: File
    get() = reportsDir.resolve(TestingBasePlugin.TESTS_DIR_NAME)

internal fun KotlinTest.configureConventions() {
    reports.configureConventions(project, name)
    conventionMapping.map("binResultsDir") { project.testResultsDir.resolve("$name/binary") }
}

internal fun TestTaskReports.configureConventions(project: Project, name: String) {
    val htmlReport = DslObject(html)
    val xmlReport = DslObject(junitXml)

    xmlReport.conventionMapping.map("destination") { project.testResultsDir.resolve(name) }
    htmlReport.conventionMapping.map("destination") { project.testReportsDir.resolve(name) }
}