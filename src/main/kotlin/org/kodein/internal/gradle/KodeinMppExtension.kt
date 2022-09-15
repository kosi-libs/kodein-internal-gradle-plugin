package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTargetPreset
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.kodein.internal.gradle.KodeinMppExtension.Targets.JS.jsPreset

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit

typealias KodeinJvmTarget = KodeinMppExtension.KodeinTarget<KotlinJvmTarget>
typealias KodeinJsTarget = KodeinMppExtension.KodeinTarget<KotlinJsTarget>
typealias KodeinJsIrTarget = KodeinMppExtension.KodeinTarget<KotlinJsIrTarget>
typealias KodeinNativeTarget = KodeinMppExtension.KodeinTarget<KotlinNativeTarget>
typealias KodeinAndroidTarget = KodeinMppExtension.KodeinTarget<KotlinAndroidTarget>

private val os = OperatingSystem.current()

@Suppress("MemberVisibilityCanBePrivate", "unused")
class KodeinMppExtension(val project: Project) {

    data class KodeinSourceSet internal constructor(
        val name: String,
        val dependencies: List<KodeinSourceSet> = emptyList(),
        val mainConf: SourceSetConf = {},
        val testConf: SourceSetConf = {}
    )

    object SourceSets {
        fun new(name: String, dependencies: List<KodeinSourceSet> = emptyList(), mainConf: SourceSetConf = {}, testConf: SourceSetConf = {}) =
                KodeinSourceSet(name, dependencies, mainConf, testConf)

        val allJvm = KodeinSourceSet(
            name = "allJvm",
            testConf = {
                dependencies {
                    if (project.properties["org.kodein.no-default-junit"] != "true") {
                        api("org.jetbrains.kotlin:kotlin-test-junit")
                    }
                }
            }
        )

        val allJs = KodeinSourceSet(
            name = "allJs",
            testConf = {
                dependencies {
                    api("org.jetbrains.kotlin:kotlin-test-js")
                }
            }
        )

        val allNative = KodeinSourceSet(
            name = "allNative"
        )

        val allPosix = KodeinSourceSet(
            name = "allPosix",
            dependencies = listOf(allNative)
        )

        val allDarwin = KodeinSourceSet(
            name = "allDarwin",
            dependencies = listOf(allPosix)
        )

        val allIos = KodeinSourceSet(
            name = "allIos",
            dependencies = listOf(allDarwin)
        )
    }

    val kodeinSourceSets = SourceSets

    data class KodeinTarget<T : KotlinTarget> internal constructor(
        val name: String,
        val preset: KodeinMppExtension.() -> String = { name },
        val dependencies: MutableList<KodeinSourceSet> = ArrayList(),
        val nativeBuildOn: OperatingSystem.() -> Boolean? = { null },
        val conf: TargetBuilder<T>.() -> Unit = {}
    ) {
        operator fun invoke(name: String) = copy(name = name)
    }

    object Targets {

        object JVM {
            val jvm = KodeinJvmTarget(
                name = "jvm",
                dependencies = arrayListOf(SourceSets.allJvm),
                conf = {
                    target.compilations.all {
                        kotlinOptions.jvmTarget = "1.8"
                    }
                }
            )

            val android = KodeinAndroidTarget(
                name = "android",
                dependencies = arrayListOf(SourceSets.allJvm),
                conf = {
                    target.publishLibraryVariants("debug", "release")
                    test.dependencies {
                        implementation("androidx.test.ext:junit:1.1.1")
                        implementation("androidx.test.espresso:espresso-core:3.2.0")
                    }
                }
            )

            val all = listOf(jvm, android)
        }

        val jvm = JVM

        object Native {
            val iosArm32 = KodeinNativeTarget(
                name = "iosArm32",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            val iosArm64 = KodeinNativeTarget(
                name = "iosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            val iosSimulatorArm64 = KodeinNativeTarget(
                name = "iosSimulatorArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            val iosX64 = KodeinNativeTarget(
                name = "iosX64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            val tvosArm64 = KodeinNativeTarget(
                name = "tvosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val tvosSimulatorArm64 = KodeinNativeTarget(
                name = "tvosSimulatorArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val tvosX64 = KodeinNativeTarget(
                name = "tvosX64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val watchosArm32 = KodeinNativeTarget(
                name = "watchosArm32",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val watchosArm64 = KodeinNativeTarget(
                name = "watchosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val watchosSimulatorArm64 = KodeinNativeTarget(
                name = "watchosSimulatorArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val watchosX86 = KodeinNativeTarget(
                name = "watchosX86",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            val linuxArm32Hfp = KodeinNativeTarget(
                name = "linuxArm32Hfp",
                nativeBuildOn = { isLinux },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            val linuxArm64 = KodeinNativeTarget(
                name = "linuxArm64",
                nativeBuildOn = { isLinux },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            val linuxX64 = KodeinNativeTarget(
                name = "linuxX64",
                nativeBuildOn = { isLinux },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            val macosX64 = KodeinNativeTarget(
                name = "macosX64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            val macosArm64 = KodeinNativeTarget(
                name = "macosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            val mingwX64 = KodeinNativeTarget(
                name = "mingwX64",
                nativeBuildOn = { isWindows },
                dependencies = arrayListOf(SourceSets.allNative)
            )

            val allIos = listOf(iosArm32, iosArm64, iosX64, iosSimulatorArm64)
            val allWatchos = listOf(watchosArm32, watchosArm64, watchosX86, watchosSimulatorArm64)
            val allTvos = listOf(tvosArm64, tvosX64, tvosSimulatorArm64)
            val allDarwin = allIos + allWatchos + allTvos

            val allDesktop = listOf(linuxX64, macosX64, macosArm64, mingwX64)

            val allEmbeddedLinux = listOf(linuxArm32Hfp, linuxArm64)
            val allLinux = allEmbeddedLinux + linuxX64

            val allPosix = listOf(linuxX64, macosX64, macosArm64) + allEmbeddedLinux + allDarwin

            val all = allPosix + mingwX64

            val host = when {
                os.isLinux -> linuxX64
                os.isMacOsX -> macosX64
                os.isWindows -> mingwX64
                else -> throw IllegalStateException("Unsupported OS host $os")
            }
        }

        val native = Native

        object JS {
            private fun KodeinJsTarget.ir(conf: TargetBuilder<KotlinJsIrTarget>.() -> Unit): KodeinJsIrTarget =
                KodeinJsIrTarget(
                    name = name,
                    preset = { "jsIr" },
                    dependencies = dependencies,
                    conf = conf
                )

            private fun KodeinMppExtension.jsPreset(): String {
                return when (project.properties["org.kodein.js.useCompiler"] as? String?) {
                    "ir" -> error("To target JS IR only, use \"add(kodeinTargets.js.ir.*)\"")
                    "legacy" -> "js"
                    "both", null -> "jsBoth"
                    else -> error("The property org.kodein.js.useCompiler must be \"ir\", \"legacy\", or \"both\".")
                }
            }

            val js = KodeinJsTarget(
                name = "js",
                preset = { jsPreset() },
                dependencies = arrayListOf(SourceSets.allJs),
                conf = { target.browser() ; target.nodejs() }
            )

            val webjs = KodeinJsTarget(
                name = "webjs",
                preset = { jsPreset() },
                dependencies = arrayListOf(SourceSets.allJs),
                conf = { target.browser() }
            )

            val nodejs = KodeinJsTarget(
                name = "nodejs",
                preset = { jsPreset() },
                dependencies = arrayListOf(SourceSets.allJs),
                conf = { target.nodejs() }
            )

            object IR {
                val js = JS.js.ir { target.browser(); target.nodejs() }
                val webjs = JS.webjs.ir { target.browser() }
                val nodejs = JS.webjs.ir { target.nodejs() }
                val all = listOf(js, nodejs, webjs)
            }

            val ir = IR
            val all = listOf(js, nodejs, webjs) + ir.all
        }

        val js = JS

        val all = JVM.all + Native.all + JS.all
        operator fun get(name: String) = all.firstOrNull { it.name == name } ?: error("No Kodein target named $name\n Known targets are: ${all.map { it.name }.joinToString()}.")
    }

    val kodeinTargets = Targets


    data class CPFix(
        val name: String,
        val mainTarget: KodeinTarget<*>,
        val excludedTargets: List<KodeinTarget<*>>,
        val excludedSourceSets: List<KodeinSourceSet> = emptyList()
    )

    fun KodeinSourceSet.recursiveDependencies(): List<KodeinSourceSet> = dependencies.flatMap { it.recursiveDependencies() } + this
    fun CPFix.intermediateSourceSets(): List<KodeinSourceSet> = mainTarget.dependencies.flatMap { it.recursiveDependencies() }

    private val availableCpFixes = listOf(
        CPFix(
            name = "jvm",
            mainTarget = Targets.jvm.jvm,
            excludedTargets = listOf(Targets.jvm.android)
        ),
        CPFix(
            name = "nativeHost",
            mainTarget = Targets.Native.host,
            excludedTargets = Targets.Native.all - Targets.Native.host,
            excludedSourceSets = listOf(SourceSets.allIos)
        ),
        CPFix(
            name = "ios",
            mainTarget = Targets.Native.iosX64,
            excludedTargets = Targets.Native.all - Targets.Native.iosX64
        )
    )

    val appliedCpFixes = KodeinLocalPropertiesPlugin.on(project).getAsList("classpathFixes")
        .also { if ("host" in it && "ios" in it) error("You cannot apply both host and ios classpath fixes at the same time") }

    val cpFixes = appliedCpFixes
        .map { name -> availableCpFixes.find { it.name == name } ?: error("Unknown classpath fix: $name") }
        .toMutableList()

    fun MutableList<CPFix>.update(name: String, update: (CPFix) -> CPFix) {
        val fix = find { it.name == name } ?: return
        remove(fix)
        add(update(fix))
    }

    val excludedTargets = (
        KodeinLocalPropertiesPlugin.on(project).getAsList("excludeTargets")
            .flatMap {
                when (it) {
                    "all-native" -> Targets.Native.all
                    "all-jvm" -> Targets.JVM.all
                    "all-js" -> Targets.JS.all
                    "nativeNonHost" -> Targets.Native.all - Targets.Native.host
                    else -> listOf(Targets[it])
                }
            }
        ).plus(cpFixes.flatMap { it.excludedTargets })

    internal var crossTargets = ArrayList<String>()
    internal var hostTargets = ArrayList<String>()
    internal var universalTargets = ArrayList<String>()

    fun NamedDomainObjectContainer<out KotlinSourceSet>.add(sourceSet: KodeinSourceSet): String? {
        for (fix in cpFixes) {
            if (sourceSet in fix.excludedSourceSets) return null
        }

        val name = cpFixes.find { sourceSet in it.intermediateSourceSets() } ?.mainTarget?.name ?: sourceSet.name

        val main = maybeCreate(name + "Main")
        val test = maybeCreate(name + "Test")

        sourceSet.mainConf(main, this)
        sourceSet.testConf(test, this)

        if (sourceSet.dependencies.isEmpty()) {
            main.dependsOn(getByName("commonMain"))
            test.dependsOn(getByName("commonTest"))
        } else {
            sourceSet.dependencies.forEach { dep ->
                add(dep)?.takeIf { it != name } ?.let {
                    main.dependsOn(getByName(it + "Main"))
                    test.dependsOn(getByName(it + "Test"))
                }
            }
        }

        return name
    }


    interface SourceSetBuilder {
        val main: KotlinSourceSet
        val test: KotlinSourceSet
        fun main(block: KotlinSourceSet.() -> Unit) { main.apply(block) }
        fun test(block: KotlinSourceSet.() -> Unit) { test.apply(block) }
        fun dependsOn(sourceSet: KodeinSourceSet)
    }

    private inner class SourceSetBuilderImpl(val sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, val name: String) : SourceSetBuilder {
        override val main: KotlinSourceSet = sourceSets.maybeCreate(name + "Main")
        override val test: KotlinSourceSet = sourceSets.maybeCreate(name + "Test")
        override fun dependsOn(sourceSet: KodeinSourceSet) {
            val dependency = sourceSets.add(sourceSet)
            if (dependency != name) {
                main.dependsOn(sourceSets[dependency + "Main"])
                test.dependsOn(sourceSets[dependency + "Test"])
            }
        }
    }

    interface TargetConfigurator<T : KotlinTarget> {
        val mainCommonCompilation: KotlinCompilation<KotlinCommonOptions>
        val testCommonCompilation: KotlinCompilation<KotlinCommonOptions>
        val target: T
        fun target(block: T.() -> Unit) { target.block() }
    }

    @Suppress("UNCHECKED_CAST")
    val <C : KotlinCompilation<*>, T : KotlinOnlyTarget<C>> TargetConfigurator<T>.mainCompilation get() = mainCommonCompilation as C
    @Suppress("UNCHECKED_CAST")
    val <C : KotlinCompilation<*>, T : KotlinOnlyTarget<C>> TargetConfigurator<T>.testCompilation get() = testCommonCompilation as C

    private class TargetConfiguratorImpl<T : KotlinTarget>(override val target: T) : TargetConfigurator<T> {
        override val mainCommonCompilation get() = target.compilations["main"]!!
        override val testCommonCompilation get() = target.compilations["test"]!!
    }

    interface TargetBuilder<T : KotlinTarget> : SourceSetBuilder, TargetConfigurator<T>

    private inner class TargetBuilderImpl<T : KotlinTarget> private constructor(val ssb: SourceSetBuilder, val tc: TargetConfigurator<T>) : TargetBuilder<T>, SourceSetBuilder by ssb, TargetConfigurator<T> by tc {
        constructor(sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, target: T, sourceSetName: String = target.name) : this(SourceSetBuilderImpl(sourceSets, sourceSetName), TargetConfiguratorImpl(target))
    }

    fun <T : KotlinTarget> KotlinMultiplatformExtension.add(target: KodeinTarget<T>, conf: TargetBuilder<T>.() -> Unit = {}) {
        if (excludedTargets.any { it.name == target.name }) {
            project.logger.warn("Target ${target.name} excluded.")
            return
        }

        val ktTarget = targets.findByName(target.name) ?: run {
            val preset = target.preset(this@KodeinMppExtension)
            @Suppress("UNCHECKED_CAST")
            val ktPreset = (presets.findByName(preset) ?: throw IllegalArgumentException("Unknown preset $preset")) as KotlinTargetPreset<T>
            val ktTarget = ktPreset.createTarget(target.name).also { targets.add(it) }

            cpFixes.filter { it.mainTarget == target }
                .forEach { fix ->
                    fix.intermediateSourceSets().forEach { iss ->
                        sourceSets.getByName(target.name + "Main").kotlin.srcDir("src/${iss.name}Main/kotlin")
                        sourceSets.getByName(target.name + "Test").kotlin.srcDir("src/${iss.name}Test/kotlin")
                        iss.mainConf(sourceSets.getByName(target.name + "Main"), sourceSets)
                        iss.testConf(sourceSets.getByName(target.name + "Test"), sourceSets)
                    }
                }

            target.dependencies.forEach {
                val depName = sourceSets.add(it)
                if (depName != null && depName != target.name) {
                    sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.getByName(depName + "Main"))
                    sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.getByName(depName + "Test"))
                }
            }

            when (target.nativeBuildOn(OperatingSystem.current())) {
                true -> hostTargets.add(target.name)
                false -> crossTargets.add(target.name)
                null -> universalTargets.add(target.name)
            }

            ktTarget
        }
        @Suppress("UNCHECKED_CAST")
        TargetBuilderImpl(sourceSets, ktTarget as T).apply(target.conf).apply(conf)
    }

    fun <T : KotlinTarget> KotlinMultiplatformExtension.add(targets: Iterable<KodeinTarget<T>>, conf: TargetBuilder<T>.() -> Unit = {}) =
            targets.forEach { add(it, conf) }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget<*>, conf: SourceSetBuilder.() -> Unit) {
        if (excludedTargets.any { it.name == target.name }) return //TODO: remove this once IDEA correctly handles intermediate source sets.
        SourceSetBuilderImpl(sourceSets, target.name).apply(conf)
    }

    fun KotlinMultiplatformExtension.sourceSet(sourceSet: KodeinSourceSet, conf: SourceSetBuilder.() -> Unit) {
        for (fix in cpFixes) {
            if (sourceSet in fix.excludedSourceSets) return
            if (sourceSet in fix.intermediateSourceSets()) {
                add(fix.mainTarget, conf)
                return
            }
        }

        val name = sourceSets.add(sourceSet)
        if (name != null)
            SourceSetBuilderImpl(sourceSets, name).conf()
    }

    fun KotlinMultiplatformExtension.sourceSet(name: String, conf: SourceSetBuilder.() -> Unit) {
        SourceSetBuilderImpl(sourceSets, name).conf()
    }

    fun KotlinMultiplatformExtension.common(conf: TargetBuilder<KotlinOnlyTarget<KotlinCommonCompilation>>.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        TargetBuilderImpl(sourceSets, targets["metadata"] as KotlinOnlyTarget<KotlinCommonCompilation>, "common").apply(conf)
    }

    val KotlinMultiplatformExtension.common get(): SourceSetBuilder = SourceSetBuilderImpl(sourceSets, "common")

    fun KotlinTarget.setCompileClasspath() {
        val targetName = name
        project.configurations.create("compileClasspath") {
            extendsFrom(project.configurations.getByName(targetName + "CompileClasspath"))
        }
    }

    fun KotlinMultiplatformExtension.allTargets(conf: TargetConfigurator<out KotlinTarget>.() -> Unit) {
        targets.forEach {
            if (it !is KotlinOnlyTarget<*>) return@forEach
            TargetConfiguratorImpl(it).conf()
        }
    }
}
