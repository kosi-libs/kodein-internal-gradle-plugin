package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.kpm.external.ExternalVariantApi
import org.jetbrains.kotlin.gradle.kpm.external.project
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.targets.native.KotlinNativeBinaryTestRun

public typealias KodeinTarget = KodeinMppExtension.Target<out KotlinTarget, out KotlinCompilation<KotlinCommonOptions>, out KotlinCommonOptions, out KodeinMppExtension.Sources>
public typealias KodeinTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinTarget, out KotlinCompilation<KotlinCommonOptions>, out KotlinCommonOptions, out KodeinMppExtension.Sources>

public typealias KodeinJvmTarget = KodeinMppExtension.Target<KotlinJvmTarget, KotlinJvmCompilation, KotlinJvmOptions, KodeinMppExtension.Sources>
public typealias KodeinJvmTargetBuilder = KodeinMppExtension.TargetBuilder<KotlinJvmTarget, KotlinJvmCompilation, KotlinJvmOptions, KodeinMppExtension.Sources>

public typealias KodeinJsTarget = KodeinMppExtension.Target<KotlinJsTargetDsl, KotlinJsCompilation, KotlinJsOptions, KodeinMppExtension.Sources>
public typealias KodeinJsTargetBuilder = KodeinMppExtension.TargetBuilder<KotlinJsTargetDsl, KotlinJsCompilation, KotlinJsOptions, KodeinMppExtension.Sources>

public typealias KodeinWasmTarget = KodeinMppExtension.Target<KotlinWasmTargetDsl, KotlinJsCompilation, KotlinJsOptions, KodeinMppExtension.Sources>
public typealias KodeinWasmTargetBuilder = KodeinMppExtension.TargetBuilder<KotlinWasmTargetDsl, KotlinJsCompilation, KotlinJsOptions, KodeinMppExtension.Sources>

public typealias KodeinNativeTarget = KodeinMppExtension.Target<out KotlinNativeTarget, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinNativeTarget, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>

public typealias KodeinNativeTargetWithHostTests = KodeinMppExtension.Target<KotlinNativeTargetWithHostTests, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetWithHostTestsBuilder = KodeinMppExtension.TargetBuilder<KotlinNativeTargetWithHostTests, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>

public typealias KodeinNativeTargetWithSimulatorTests = KodeinMppExtension.Target<KotlinNativeTargetWithSimulatorTests, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetWithSimulatorTestsBuilder = KodeinMppExtension.TargetBuilder<KotlinNativeTargetWithSimulatorTests, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>

public typealias KodeinNativeTargetWithTests = KodeinMppExtension.Target<out KotlinNativeTargetWithTests<out KotlinNativeBinaryTestRun>, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetWithTestsBuilder = KodeinMppExtension.TargetBuilder<out KotlinNativeTargetWithTests<out KotlinNativeBinaryTestRun>, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>

public open class KodeinMppExtension(internal val kotlin: KotlinMultiplatformExtension) {

    @OptIn(ExternalVariantApi::class)
    internal val project get() = kotlin.project

    public open inner class Sources(internal val name: String) {
        public val main: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "Main")
        public open val test: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "Test")
        public fun main(configure: KotlinSourceSet.() -> Unit) { main.configure(configure) }
        public fun mainDependencies(configure: KotlinDependencyHandler.() -> Unit) { main.configure { dependencies(configure) } }
        public fun test(configure: KotlinSourceSet.() -> Unit) { test.configure(configure) }
        public fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) { test.configure { dependencies(configure) } }
    }

    public val common: Sources get() = Sources("common")
    public fun common(configure: Sources.() -> Unit) { common.apply(configure) }

    public inner class TargetBuilder<T : KotlinTarget, C : KotlinCompilation<O>, O : KotlinCommonOptions, S : Sources> internal constructor(
            public val target: T,
            public val sources: S
    ) {
        public fun target(block: T.() -> Unit) { target.block() }

        public inner class Compilations(container: NamedDomainObjectContainer<C>) : NamedDomainObjectContainer<C> by container {
            public val main: NamedDomainObjectProvider<C> get() = named("main")
            public val test: NamedDomainObjectProvider<C> get() = named("test")
            public fun main(configure: C.() -> Unit) { main.configure(configure) }
            public fun test(configure: C.() -> Unit) { test.configure(configure) }
        }
        @Suppress("UNCHECKED_CAST")
        public val compilations: Compilations get() = Compilations(target.compilations as NamedDomainObjectContainer<C>)

        public fun KotlinJvmTarget.setCompileClasspath() {
            val targetName = name
            project.configurations.create("compileClasspath") {
                extendsFrom(project.configurations.getByName(targetName + "CompileClasspath"))
            }
        }
    }

    public class Target<T : KotlinTarget, C : KotlinCompilation<O>, O : KotlinCommonOptions, S : Sources>(
            internal val name: String,
            internal val kotlinAccess: KotlinMultiplatformExtension.(String) -> T,
            internal val sourceBuilder: (String) -> S,
            internal val nativeBuildOn: OperatingSystem.() -> Boolean? = { null },
            internal val defaultConfig: TargetBuilder<T, C, O, S>.() -> Unit = {}
    ) {
        internal fun access(kotlin: KotlinMultiplatformExtension) = kotlinAccess(kotlin, name)
    }

    internal open inner class Targets {
        fun <T : KotlinTarget, C : KotlinCompilation<O>, O : KotlinCommonOptions> Target(
                name: String,
                kotlinAccess: KotlinMultiplatformExtension.(String) -> T,
                nativeBuildOn: OperatingSystem.() -> Boolean? = { null },
                defaultConfig: TargetBuilder<T, C, O, Sources>.() -> Unit = {}
        ): Target<T, C, O, Sources> = Target(name, kotlinAccess, ::Sources, nativeBuildOn, defaultConfig)

        fun <C : KotlinCompilation<O>, O : KotlinCommonOptions> TargetBuilder<*, C, O, out Sources>.commonJvmConfig(
            compilerOptions: C.() -> HasCompilerOptions<KotlinJvmCompilerOptions>
        ) {
            compilations.configureEach {
                compilerOptions(this).configure {
                    jvmTarget.set(KodeinJvmPlugin.jvmTarget(project))
                }
            }
            if (target.project.properties["org.kodein.no-default-junit"] != "true") {
                sources.testDependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test-junit")
                }
            }
        }

        val jvm: KodeinJvmTarget = Target("jvm", KotlinMultiplatformExtension::jvm) { commonJvmConfig(KotlinJvmCompilation::compilerOptions) }

        var jsEnvBrowser = true
        var jsEnvNodejs = true
        var jsEnvD8 = true
        var jsConfigured = false
        val js: KodeinJsTarget = Target("js", { js(it, IR) }) {
            jsConfigured = true
            if (jsEnvBrowser) target.browser()
            if (jsEnvNodejs) target.nodejs()
        }
        @OptIn(ExperimentalWasmDsl::class)
        val wasm: KodeinWasmTarget = Target("wasm", { wasm(it) as KotlinWasmTargetDsl }) {
            jsConfigured = true
            if (jsEnvBrowser) target.browser {
                // TODO: Remove with Kotlin 1.9.0
                // https://youtrack.jetbrains.com/issue/KT-56159/Running-karma-tests-doesnt-work-in-a-project-generated-by-wizard-Browser-Application-for-Kotlin-Wasm
                testTask {
                    useKarma {
                        webpackConfig.experiments.add("topLevelAwait")
                        useChromeHeadless()
                    }
                }

            }
            // TODO: Try again with Kotlin 1.9.0
            // This is failing in Kosi-Kaverit (test fail only in Node)
            // if (jsEnvNodejs) target.nodejs()
            if (jsEnvD8) target.d8()
        }

        val linuxX64: KodeinNativeTargetWithHostTests = Target("linuxX64", KotlinMultiplatformExtension::linuxX64, nativeBuildOn = { isLinux })
        val linuxArm64: KodeinNativeTarget = Target("linuxArm64", KotlinMultiplatformExtension::linuxArm64, nativeBuildOn = { isLinux })
        val allLinux = listOf(linuxX64, linuxArm64)

        val mingwX64: KodeinNativeTargetWithHostTests = Target("mingwX64", KotlinMultiplatformExtension::mingwX64, nativeBuildOn = { isWindows })

        val macosX64: KodeinNativeTargetWithHostTests = Target("macosX64", KotlinMultiplatformExtension::macosX64, nativeBuildOn = { isMacOsX })
        val macosArm64: KodeinNativeTargetWithHostTests = Target("macosArm64", KotlinMultiplatformExtension::macosArm64, nativeBuildOn = { isMacOsX })
        val allMacos = listOf(macosX64, macosArm64)

        val allDesktop = allMacos + listOf(linuxX64, mingwX64)

        val androidNativeX64: KodeinNativeTarget = Target("androidNativeX64", KotlinMultiplatformExtension::androidNativeX64, nativeBuildOn = { isLinux })
        val androidNativeX86: KodeinNativeTarget = Target("androidNativeX86", KotlinMultiplatformExtension::androidNativeX86, nativeBuildOn = { isLinux })
        val androidNativeArm64: KodeinNativeTarget = Target("androidNativeArm64", KotlinMultiplatformExtension::androidNativeArm64, nativeBuildOn = { isLinux })
        val androidNativeArm32: KodeinNativeTarget = Target("androidNativeArm32", KotlinMultiplatformExtension::androidNativeArm32, nativeBuildOn = { isLinux })
        val allAndroidNative = listOf(androidNativeX64, androidNativeX86, androidNativeArm64, androidNativeArm32)

        val iosX64: KodeinNativeTargetWithSimulatorTests = Target("iosX64", KotlinMultiplatformExtension::iosX64, nativeBuildOn = { isMacOsX })
        val iosSimulatorArm64: KodeinNativeTargetWithSimulatorTests = Target("iosSimulatorArm64", KotlinMultiplatformExtension::iosSimulatorArm64, nativeBuildOn = { isMacOsX })
        val iosArm64: KodeinNativeTarget = Target("iosArm64", KotlinMultiplatformExtension::iosArm64, nativeBuildOn = { isMacOsX })
        val allIos = listOf(iosX64, iosSimulatorArm64, iosArm64)

        val tvosX64: KodeinNativeTargetWithSimulatorTests = Target("tvosX64", KotlinMultiplatformExtension::tvosX64, nativeBuildOn = { isMacOsX })
        val tvosSimulatorArm64: KodeinNativeTargetWithSimulatorTests = Target("tvosSimulatorArm64", KotlinMultiplatformExtension::tvosSimulatorArm64, nativeBuildOn = { isMacOsX })
        val tvosArm64: KodeinNativeTarget = Target("tvosArm64", KotlinMultiplatformExtension::tvosArm64, nativeBuildOn = { isMacOsX })
        val allTvos = listOf(tvosX64, tvosSimulatorArm64, tvosArm64)

        val watchosX64: KodeinNativeTargetWithSimulatorTests = Target("watchosX64", KotlinMultiplatformExtension::watchosX64, nativeBuildOn = { isMacOsX })
        val watchosSimulatorArm64: KodeinNativeTargetWithSimulatorTests = Target("watchosSimulatorArm64", KotlinMultiplatformExtension::watchosSimulatorArm64, nativeBuildOn = { isMacOsX })
        val watchosArm64: KodeinNativeTarget = Target("watchosArm64", KotlinMultiplatformExtension::watchosArm64, nativeBuildOn = { isMacOsX })
        val watchosArm32: KodeinNativeTarget = Target("watchosArm32", KotlinMultiplatformExtension::watchosArm32, nativeBuildOn = { isMacOsX })
        val watchosDeviceArm64: KodeinNativeTarget = Target("watchosDeviceArm64", KotlinMultiplatformExtension::watchosDeviceArm64, nativeBuildOn = { isMacOsX })
        val allWatchos = listOf(watchosX64, watchosSimulatorArm64, watchosArm64, watchosArm32, watchosDeviceArm64)

        val allAppleMobile = allIos + allTvos + allWatchos
        val allApple = allMacos + allAppleMobile

        val allEmbedded = allAndroidNative + linuxArm64

        val allNative = allMacos + allLinux + mingwX64 + allAndroidNative + allAppleMobile

        val allPosix = allNative - mingwX64

        val allNativeTier1 = listOf(linuxX64, macosX64, macosArm64, iosSimulatorArm64, iosX64)
        val allNativeTier1And2 = allNativeTier1 + listOf(linuxArm64, watchosSimulatorArm64, watchosX64, watchosArm32, watchosArm64, tvosSimulatorArm64, tvosX64, tvosArm64, iosArm64)

        open val all: List<KodeinTarget> = allNative + jvm + js + wasm
    }

    internal open val targets = Targets()

    private val currentOS = OperatingSystem.current()

    internal val excludedTargets: List<String> = (
            KodeinLocalPropertiesPlugin.on(project).getAsList("excludeTargets")
                .flatMap { targetName ->
                    when (targetName) {
                        "all-native" -> targets.allNative.map { it.name }
                        "all-jvm" -> listOf("jvm", "android")
                        "all-js" -> listOf("js", "wasm")
                        "nativeNonHost" -> targets.allNative.map { it.name } - when {
                            currentOS.isLinux -> "linuxX64"
                            currentOS.isMacOsX -> "macosX64"
                            currentOS.isWindows -> "mingwX64"
                            else -> throw IllegalStateException("Unsupported OS host $currentOS")
                        }
                        else -> listOf(targetName)
                    }
                }
            )

    internal var hostTargets = HashSet<String>()
    internal var crossTargets = HashSet<String>()

    private val created = HashSet<String>()

    public fun <T : KotlinTarget, C : KotlinCompilation<O>, O : KotlinCommonOptions, S : Sources> add(target: Target<T, C, O, S>, configure: TargetBuilder<T, C, O, S>.() -> Unit) {
        if (target.name in excludedTargets) {
            project.logger.warn("Target ${target.name} excluded.")
            return
        }

        val targetBuilder = TargetBuilder<T, C, O, S>(target.access(kotlin), target.sourceBuilder(target.name))

        if (target.name !in created) {
            created += target.name
            target.defaultConfig(targetBuilder)

            when (target.nativeBuildOn(currentOS)) {
                true -> hostTargets.add(target.name)
                false -> crossTargets.add(target.name)
                null -> {}
            }
        }

        targetBuilder.apply(configure)
    }

    public fun jvm(configure: KodeinJvmTargetBuilder.() -> Unit = {}): Unit = add(targets.jvm, configure)

    public fun jsEnv(browser: Boolean = targets.jsEnvBrowser, nodejs: Boolean = targets.jsEnvNodejs, d8: Boolean = targets.jsEnvD8) {
        check(!targets.jsConfigured) { "Please call jsEnv *before* creating JS or WASM targets" }
        targets.jsEnvBrowser = browser
        targets.jsEnvNodejs = nodejs
        targets.jsEnvD8 = d8
    }
    public fun jsEnvBrowserOnly(): Unit = jsEnv(browser = true, nodejs = false, d8 = false)
    public fun js(configure: KodeinJsTargetBuilder.() -> Unit = {}): Unit = add(targets.js, configure)
    public fun wasm(configure: KodeinWasmTargetBuilder.() -> Unit = {}): Unit = add(targets.wasm, configure)

    public fun linuxX64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.linuxX64, configure)
    public fun linuxArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.linuxArm64, configure)
    public fun allLinux(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allLinux.forEach { add(it, configure) }

    public fun mingwX64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.mingwX64, configure)

    public fun macosX64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.macosX64, configure)
    public fun macosArm64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.macosArm64, configure)
    public fun allMacos(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = targets.allMacos.forEach { add(it, configure) }

    public fun allDesktop(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = targets.allDesktop.forEach { add(it, configure) }

    public fun androidNativeX64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeX64, configure)
    public fun androidNativeX86(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeX86, configure)
    public fun androidNativeArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeArm64, configure)
    public fun androidNativeArm32(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeArm32, configure)
    public fun allAndroidNative(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allAndroidNative.forEach { add(it, configure) }

    public fun iosX64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.iosX64, configure)
    public fun iosSimulatorArm64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.iosSimulatorArm64, configure)
    public fun iosArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.iosArm64, configure)
    public fun allIos(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allIos.forEach { add(it, configure) }

    public fun tvosX64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.tvosX64, configure)
    public fun tvosSimulatorArm64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.tvosSimulatorArm64, configure)
    public fun tvosArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.tvosArm64, configure)
    public fun allTvos(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allTvos.forEach { add(it, configure) }

    public fun watchosX64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.watchosX64, configure)
    public fun watchosSimulatorArm64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.watchosSimulatorArm64, configure)
    public fun watchosArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.watchosArm64, configure)
    public fun watchosArm32(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.watchosArm32, configure)
    public fun watchosDeviceArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.watchosDeviceArm64, configure)
    public fun allWatchos(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allWatchos.forEach { add(it, configure) }

    public fun allAppleMobile(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allAppleMobile.forEach { add(it, configure) }
    public fun allApple(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allApple.forEach { add(it, configure) }

    public fun allEmbedded(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allEmbedded.forEach { add(it, configure) }

    public fun allNative(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allNative.forEach { add(it, configure) }

    public fun allPosix(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allPosix.forEach { add(it, configure) }

    public fun allNativeTier1(configure: KodeinNativeTargetWithTestsBuilder.() -> Unit = {}): Unit = targets.allNativeTier1.forEach { add(it, configure) }
    public fun allNativeTierTo2(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = targets.allNativeTier1And2.forEach { add(it, configure) }

    public fun all(configure: KodeinTargetBuilder.() -> Unit = {}): Unit = targets.all.forEach { add(it, configure) }
}
