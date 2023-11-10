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
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmWasiTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.targets.native.KotlinNativeBinaryTestRun

public typealias KodeinTarget = KodeinMppExtension.Target<out KotlinTarget, out KotlinCompilation<KotlinCommonOptions>, out KotlinCommonOptions, out KodeinMppExtension.Sources>
public typealias KodeinTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinTarget, out KotlinCompilation<KotlinCommonOptions>, out KotlinCommonOptions, out KodeinMppExtension.Sources>

public typealias KodeinJvmTarget = KodeinMppExtension.Target<KotlinJvmTarget, KotlinJvmCompilation, KotlinJvmOptions, KodeinMppExtension.Sources>
public typealias KodeinJvmTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinJvmTarget, out KotlinJvmCompilation, out KotlinJvmOptions, out KodeinMppExtension.Sources>

public typealias KodeinJsTarget = KodeinMppExtension.Target<KotlinJsTargetDsl, KotlinJsCompilation, KotlinJsOptions, KodeinMppExtension.Sources>
public typealias KodeinJsTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinJsTargetDsl, out KotlinJsCompilation, out KotlinJsOptions, out KodeinMppExtension.Sources>

public typealias KodeinWasmTarget<T> = KodeinMppExtension.Target<T, KotlinJsCompilation, KotlinJsOptions, KodeinMppExtension.Sources>
public typealias KodeinWasmTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinWasmTargetDsl, out KotlinJsCompilation, out KotlinJsOptions, out KodeinMppExtension.Sources>

public typealias KodeinNativeTarget = KodeinMppExtension.Target<out KotlinNativeTarget, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetBuilder = KodeinMppExtension.TargetBuilder<out KotlinNativeTarget, out KotlinNativeCompilation, out KotlinCommonOptions, out KodeinMppExtension.Sources>

public typealias KodeinNativeTargetWithHostTests = KodeinMppExtension.Target<out KotlinNativeTargetWithHostTests, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetWithHostTestsBuilder = KodeinMppExtension.TargetBuilder<out KotlinNativeTargetWithHostTests, out KotlinNativeCompilation, out KotlinCommonOptions, out KodeinMppExtension.Sources>

public typealias KodeinNativeTargetWithSimulatorTests = KodeinMppExtension.Target<KotlinNativeTargetWithSimulatorTests, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetWithSimulatorTestsBuilder = KodeinMppExtension.TargetBuilder<out KotlinNativeTargetWithSimulatorTests, out KotlinNativeCompilation, out KotlinCommonOptions, out KodeinMppExtension.Sources>

public typealias KodeinNativeTargetWithTests = KodeinMppExtension.Target<out KotlinNativeTargetWithTests<out KotlinNativeBinaryTestRun>, KotlinNativeCompilation, KotlinCommonOptions, KodeinMppExtension.Sources>
public typealias KodeinNativeTargetWithTestsBuilder = KodeinMppExtension.TargetBuilder<out KotlinNativeTargetWithTests<out KotlinNativeBinaryTestRun>, out KotlinNativeCompilation, out KotlinCommonOptions, out KodeinMppExtension.Sources>

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

    public open inner class Targets {
        public fun <T : KotlinTarget, C : KotlinCompilation<O>, O : KotlinCommonOptions> Target(
                name: String,
                kotlinAccess: KotlinMultiplatformExtension.(String) -> T,
                nativeBuildOn: OperatingSystem.() -> Boolean? = { null },
                defaultConfig: TargetBuilder<T, C, O, Sources>.() -> Unit = {}
        ): Target<T, C, O, Sources> = Target(name, kotlinAccess, ::Sources, nativeBuildOn, defaultConfig)

        public fun <C : KotlinCompilation<O>, O : KotlinCommonOptions> TargetBuilder<*, C, O, out Sources>.commonJvmConfig(
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

        public val jvm: KodeinJvmTarget = Target("jvm", KotlinMultiplatformExtension::jvm) { commonJvmConfig(KotlinJvmCompilation::compilerOptions) }

        internal var jsEnvBrowser = true
        internal var jsEnvNodejs = true
        internal var jsEnvD8 = true
        internal var jsConfigured = false
        public val js: KodeinJsTarget = Target("js", { js(it, IR) }) {
            jsConfigured = true
            if (jsEnvBrowser) target.browser()
            if (jsEnvNodejs) target.nodejs()
        }
        // TODO Review with Salomon
        @OptIn(ExperimentalWasmDsl::class)
        public val wasmJs: KodeinWasmTarget<KotlinWasmJsTargetDsl> = Target("wasmJs", { wasmJs(it) }) {
            jsConfigured = true
            if (jsEnvD8) target.d8()
             if (jsEnvBrowser) target.browser {
                 // Because Chrome 11* on CI is not compatible
                 // https://youtrack.jetbrains.com/issue/KT-63014
                 testTask { enabled = false }
             }
            // TODO: Try again with Kotlin 2.0.0
            // This is failing in Kosi-Kaverit (test fail in Node)
            // if (jsEnvNodejs) target.nodejs()
        }
        @OptIn(ExperimentalWasmDsl::class)
        public val wasmWasi: KodeinWasmTarget<KotlinWasmWasiTargetDsl> = Target("wasmWasi", { wasmWasi(it) }) {
                jsConfigured = true
                // TODO: Try again with Kotlin 2.0.0
                // This is failing in Kosi-Kaverit (test fail in Node)
                // if (jsEnvNodejs) target.nodejs()
        }
        public val allWasm: List<KodeinWasmTarget<out KotlinWasmTargetDsl>> get() = listOf(wasmJs, wasmWasi)

        public val linuxX64: KodeinNativeTargetWithHostTests = Target("linuxX64", KotlinMultiplatformExtension::linuxX64, nativeBuildOn = { isLinux })
        public val linuxArm64: KodeinNativeTarget = Target("linuxArm64", KotlinMultiplatformExtension::linuxArm64, nativeBuildOn = { isLinux })
        public val allLinux: List<KodeinNativeTarget> get() = listOf(linuxX64, linuxArm64)

        public val mingwX64: KodeinNativeTargetWithHostTests = Target("mingwX64", KotlinMultiplatformExtension::mingwX64, nativeBuildOn = { isWindows })

        public val macosX64: KodeinNativeTargetWithHostTests = Target("macosX64", KotlinMultiplatformExtension::macosX64, nativeBuildOn = { isMacOsX })
        public val macosArm64: KodeinNativeTargetWithHostTests = Target("macosArm64", KotlinMultiplatformExtension::macosArm64, nativeBuildOn = { isMacOsX })
        public val allMacos: List<KodeinNativeTargetWithHostTests> get() = listOf(macosX64, macosArm64)

        public val allDesktop: List<KodeinNativeTargetWithHostTests> get() = allMacos + listOf(linuxX64, mingwX64)

        public val androidNativeX64: KodeinNativeTarget = Target("androidNativeX64", KotlinMultiplatformExtension::androidNativeX64, nativeBuildOn = { isLinux })
        public val androidNativeX86: KodeinNativeTarget = Target("androidNativeX86", KotlinMultiplatformExtension::androidNativeX86, nativeBuildOn = { isLinux })
        public val androidNativeArm64: KodeinNativeTarget = Target("androidNativeArm64", KotlinMultiplatformExtension::androidNativeArm64, nativeBuildOn = { isLinux })
        public val androidNativeArm32: KodeinNativeTarget = Target("androidNativeArm32", KotlinMultiplatformExtension::androidNativeArm32, nativeBuildOn = { isLinux })
        public val allAndroidNative: List<KodeinNativeTarget> get() = listOf(androidNativeX64, androidNativeX86, androidNativeArm64, androidNativeArm32)

        public val iosX64: KodeinNativeTargetWithSimulatorTests = Target("iosX64", KotlinMultiplatformExtension::iosX64, nativeBuildOn = { isMacOsX })
        public val iosSimulatorArm64: KodeinNativeTargetWithSimulatorTests = Target("iosSimulatorArm64", KotlinMultiplatformExtension::iosSimulatorArm64, nativeBuildOn = { isMacOsX })
        public val iosArm64: KodeinNativeTarget = Target("iosArm64", KotlinMultiplatformExtension::iosArm64, nativeBuildOn = { isMacOsX })
        public val allIos: List<KodeinNativeTarget> get() = listOf(iosX64, iosSimulatorArm64, iosArm64)

        public val tvosX64: KodeinNativeTargetWithSimulatorTests = Target("tvosX64", KotlinMultiplatformExtension::tvosX64, nativeBuildOn = { isMacOsX })
        public val tvosSimulatorArm64: KodeinNativeTargetWithSimulatorTests = Target("tvosSimulatorArm64", KotlinMultiplatformExtension::tvosSimulatorArm64, nativeBuildOn = { isMacOsX })
        public val tvosArm64: KodeinNativeTarget = Target("tvosArm64", KotlinMultiplatformExtension::tvosArm64, nativeBuildOn = { isMacOsX })
        public val allTvos: List<KodeinNativeTarget> get() = listOf(tvosX64, tvosSimulatorArm64, tvosArm64)

        public val watchosX64: KodeinNativeTargetWithSimulatorTests = Target("watchosX64", KotlinMultiplatformExtension::watchosX64, nativeBuildOn = { isMacOsX })
        public val watchosSimulatorArm64: KodeinNativeTargetWithSimulatorTests = Target("watchosSimulatorArm64", KotlinMultiplatformExtension::watchosSimulatorArm64, nativeBuildOn = { isMacOsX })
        public val watchosArm64: KodeinNativeTarget = Target("watchosArm64", KotlinMultiplatformExtension::watchosArm64, nativeBuildOn = { isMacOsX })
        public val watchosArm32: KodeinNativeTarget = Target("watchosArm32", KotlinMultiplatformExtension::watchosArm32, nativeBuildOn = { isMacOsX })
        public val watchosDeviceArm64: KodeinNativeTarget = Target("watchosDeviceArm64", KotlinMultiplatformExtension::watchosDeviceArm64, nativeBuildOn = { isMacOsX })
        public val allWatchos: List<KodeinNativeTarget> get() = listOf(watchosX64, watchosSimulatorArm64, watchosArm64, watchosArm32, watchosDeviceArm64)
        public val allWatchosNoDevice: List<KodeinNativeTarget> get() = listOf(watchosX64, watchosSimulatorArm64, watchosArm64, watchosArm32)

        public val allAppleMobile: List<KodeinNativeTarget> get() = allIos + allTvos + allWatchos
        public val allApple: List<KodeinNativeTarget> get() = allMacos + allAppleMobile

        public val allEmbedded: List<KodeinNativeTarget> get() = allAndroidNative + linuxArm64

        public val allNative: List<KodeinNativeTarget> get() = allMacos + allLinux + mingwX64 + allAndroidNative + allAppleMobile

        public val allPosix: List<KodeinNativeTarget> get() = allNative - mingwX64

        public val allNativeTier1: List<KodeinNativeTargetWithTests> get() = listOf(linuxX64, macosX64, macosArm64, iosSimulatorArm64, iosX64)
        public val allNativeTier1And2: List<KodeinNativeTarget> get() = allNativeTier1 + listOf(linuxArm64, watchosSimulatorArm64, watchosX64, watchosArm32, watchosArm64, tvosSimulatorArm64, tvosX64, tvosArm64, iosArm64)

        public open val all: List<KodeinTarget> get() = allNative + jvm + js + allWasm

        public open val allComposeStable: List<KodeinTarget> get() = allDesktop + allIos + allTvos + allWatchosNoDevice + jvm + js
        public val allComposeExperimental: List<KodeinTarget> get() = allComposeStable + wasmJs

        public open val allTestable: List<KodeinTarget> get() = allDesktop + iosX64 + iosSimulatorArm64 + tvosX64 + tvosSimulatorArm64 + watchosX64 + watchosSimulatorArm64 + jvm + js
    }

    public open val targets: Targets = Targets()

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

    public fun <T : KotlinTarget, C : KotlinCompilation<O>, O : KotlinCommonOptions, S : Sources> addAll(targets: List<Target<out T, out C, out O, out S>>, configure: TargetBuilder<out T, out C, out O, out S>.() -> Unit) {
        targets.forEach { add(it, configure) }
    }

    public fun jvm(configure: KodeinJvmTargetBuilder.() -> Unit = {}): Unit = add(targets.jvm) { configure() }

    public fun jsEnv(browser: Boolean = targets.jsEnvBrowser, nodejs: Boolean = targets.jsEnvNodejs, d8: Boolean = targets.jsEnvD8) {
        check(!targets.jsConfigured) { "Please call jsEnv *before* creating JS or WASM targets" }
        targets.jsEnvBrowser = browser
        targets.jsEnvNodejs = nodejs
        targets.jsEnvD8 = d8
    }
    public fun jsEnvBrowserOnly(): Unit = jsEnv(browser = true, nodejs = false, d8 = false)
    public fun js(configure: KodeinJsTargetBuilder.() -> Unit = {}): Unit = add(targets.js) { configure() }
    @Deprecated("Use wasmJs instead", ReplaceWith("wasmJs(configure)"))
    public fun wasm(configure: KodeinWasmTargetBuilder.() -> Unit = {}): Unit = add(targets.wasmJs) { configure() }
    public fun wasmJs(configure: KodeinWasmTargetBuilder.() -> Unit = {}): Unit = add(targets.wasmJs) { configure() }
    public fun wasmWasi(configure: KodeinWasmTargetBuilder.() -> Unit = {}): Unit = add(targets.wasmWasi) { configure() }
    public fun allWasm(configure: KodeinWasmTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allWasm) { configure() }

    public fun linuxX64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.linuxX64) { configure() }
    public fun linuxArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.linuxArm64) { configure() }
    public fun allLinux(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allLinux) { configure() }

    public fun mingwX64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.mingwX64) { configure() }

    public fun macosX64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.macosX64) { configure() }
    public fun macosArm64(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = add(targets.macosArm64) { configure() }
    public fun allMacos(configure: KodeinNativeTargetWithHostTestsBuilder.() -> Unit = {}): Unit = addAll(targets.allMacos) { configure() }

    public fun allDesktop(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allDesktop) { configure() }

    public fun androidNativeX64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeX64) { configure() }
    public fun androidNativeX86(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeX86) { configure() }
    public fun androidNativeArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeArm64) { configure() }
    public fun androidNativeArm32(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.androidNativeArm32) { configure() }
    public fun allAndroidNative(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allAndroidNative) { configure() }

    public fun iosX64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.iosX64) { configure() }
    public fun iosSimulatorArm64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.iosSimulatorArm64) { configure() }
    public fun iosArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.iosArm64) { configure() }
    public fun allIos(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allIos) { configure() }

    public fun tvosX64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.tvosX64) { configure() }
    public fun tvosSimulatorArm64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.tvosSimulatorArm64) { configure() }
    public fun tvosArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.tvosArm64) { configure() }
    public fun allTvos(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allTvos) { configure() }

    public fun watchosX64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.watchosX64) { configure() }
    public fun watchosSimulatorArm64(configure: KodeinNativeTargetWithSimulatorTestsBuilder.() -> Unit = {}): Unit = add(targets.watchosSimulatorArm64) { configure() }
    public fun watchosArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.watchosArm64) { configure() }
    public fun watchosArm32(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.watchosArm32) { configure() }
    public fun watchosDeviceArm64(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = add(targets.watchosDeviceArm64) { configure() }
    public fun allWatchos(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allWatchos) { configure(this) }

    public fun allAppleMobile(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allAppleMobile) { configure() }
    public fun allApple(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allApple) { configure() }

    public fun allEmbedded(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allEmbedded) { configure() }

    public fun allNative(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allNative) { configure() }

    public fun allPosix(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allPosix) { configure() }

    public fun allNativeTier1(configure: KodeinNativeTargetWithTestsBuilder.() -> Unit = {}): Unit = addAll(targets.allNativeTier1) { configure() }
    public fun allNativeTierTo2(configure: KodeinNativeTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allNativeTier1) { configure() }

    public fun all(configure: KodeinTargetBuilder.() -> Unit = {}): Unit = addAll(targets.all) { configure() }

    public fun allComposeExperimental(configure: KodeinTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allComposeExperimental) { configure() }
    public fun allComposeStable(configure: KodeinTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allComposeStable) { configure() }

    public fun allTestable(configure: KodeinTargetBuilder.() -> Unit = {}): Unit = addAll(targets.allTestable) { configure() }
}
