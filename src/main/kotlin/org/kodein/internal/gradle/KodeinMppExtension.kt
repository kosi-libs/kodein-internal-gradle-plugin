package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit

@Suppress("UNUSED_TYPEALIAS_PARAMETER")
typealias KodeinJvmTarget = KodeinMppExtension.KodeinTarget<KotlinJvmTarget>
typealias KodeinJsTarget = KodeinMppExtension.KodeinTarget<KotlinJsTarget>
typealias KodeinNativeTarget = KodeinMppExtension.KodeinTarget<KotlinNativeTarget>
typealias KodeinAndroidTarget = KodeinMppExtension.KodeinTarget<KotlinAndroidTarget>

private val os = OperatingSystem.current()

@Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage")
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
                mainConf = {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
                    }
                },
                testConf = {
                    dependencies {
                        api("org.jetbrains.kotlin:kotlin-test")
                        api("org.jetbrains.kotlin:kotlin-test-junit")
                        api("junit:junit:4.12")
                    }
                }
        )

        val allJs = KodeinSourceSet(
                name = "allJs",
                mainConf = {
                    dependencies {
                        implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                    }
                },
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
            val target: String,
            val name: String = target,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val nativeBuildOn: OperatingSystem.() -> Boolean? = { null },
            val conf: TargetBuilder<T>.() -> Unit = {}
    ) {
        operator fun invoke(name: String) = copy(name = name)
    }

    object Targets {

        object JVM {
            val jvm = KodeinJvmTarget(
                    target = "jvm",
                    dependencies = listOf(SourceSets.allJvm),
                    conf = {
                        target.compilations.all {
                            compileKotlinTask.sourceCompatibility = "1.8"
                            compileKotlinTask.targetCompatibility = "1.8"
                            kotlinOptions.jvmTarget = "1.8"
                        }
                    }
            )

            val android = KodeinAndroidTarget(
                    target = "android",
                    dependencies = listOf(SourceSets.allJvm),
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
                    target = "iosArm32",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allIos)
            )

            val iosArm64 = KodeinNativeTarget(
                    target = "iosArm64",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allIos)
            )

            val iosX64 = KodeinNativeTarget(
                    target = "iosX64",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allIos)
            )

            val tvosArm64 = KodeinNativeTarget(
                    target = "tvosArm64",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allDarwin)
            )

            val tvosX64 = KodeinNativeTarget(
                    target = "tvosX64",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allDarwin)
            )

            val watchosArm32 = KodeinNativeTarget(
                    target = "watchosArm32",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allDarwin)
            )

            val watchosArm64 = KodeinNativeTarget(
                    target = "watchosArm64",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allDarwin)
            )

            val watchosX86 = KodeinNativeTarget(
                    target = "watchosX86",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allDarwin)
            )

            val linuxArm32Hfp = KodeinNativeTarget(
                    target = "linuxArm32Hfp",
                    nativeBuildOn = { isLinux },
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxMips32 = KodeinNativeTarget(
                    target = "linuxMips32",
                    nativeBuildOn = { isLinux },
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxMipsel32 = KodeinNativeTarget(
                    target = "linuxMipsel32",
                    nativeBuildOn = { isLinux },
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxX64 = KodeinNativeTarget(
                    target = "linuxX64",
                    nativeBuildOn = { isLinux },
                    dependencies = listOf(SourceSets.allPosix)
            )

            val macosX64 = KodeinNativeTarget(
                    target = "macosX64",
                    nativeBuildOn = { isMacOsX },
                    dependencies = listOf(SourceSets.allPosix)
            )

            val mingwX64 = KodeinNativeTarget(
                    target = "mingwX64",
                    nativeBuildOn = { isWindows },
                    dependencies = listOf(SourceSets.allPosix)
            )

            val allIos = listOf(iosArm32, iosArm64, iosX64)
            val allWatchos = listOf(watchosArm32, watchosArm64, watchosX86)
            val allTvos = listOf(tvosArm64, tvosX64)
            val allDarwin = allIos + allWatchos + allTvos

            val allDesktop = listOf(linuxX64, macosX64, mingwX64)

            val allEmbeddedLinux = listOf(linuxArm32Hfp, linuxMips32, linuxMipsel32)
            val allLinux = allEmbeddedLinux + linuxX64

            val allPosix = allDesktop + allEmbeddedLinux + allDarwin

            val all = allPosix

            val host = when {
                os.isLinux -> linuxX64
                os.isMacOsX -> macosX64
                os.isWindows -> mingwX64
                else -> throw IllegalStateException("Unsupported OS host $os")
            }
        }

        val native = Native

        object JS {
            val js = KodeinJsTarget(
                    target = "js",
                    dependencies = listOf(SourceSets.allJs),
                    conf = { target.browser() ; target.nodejs() }
            )

            val webjs = KodeinJsTarget(
                    target = "js",
                    name = "webjs",
                    dependencies = listOf(SourceSets.allJs),
                    conf = { target.browser() }
            )

            val nodejs = KodeinJsTarget(
                    target = "js",
                    name = "nodejs",
                    dependencies = listOf(SourceSets.allJs),
                    conf = { target.nodejs() }
            )

            val all = listOf(js, nodejs, webjs)
        }

        val js = JS

        val all = JVM.all + Native.all + JS.all
        operator fun get(name: String) = all.firstOrNull { it.target == name } ?: error("No Kodein target named $name\n Known targets are: ${all.map { it.target }.joinToString()}.")
    }

    val kodeinTargets = Targets


    data class CPFix(
            val name: String,
            val mainTarget: KodeinTarget<*>,
            val excludedTargets: List<KodeinTarget<*>>,
            val intermediateSourceSets: List<KodeinSourceSet>,
            val excludedSourceSets: List<KodeinSourceSet> = emptyList()
    )

    private val availableCpFixes = listOf(
            CPFix(
                    name = "jvm",
                    mainTarget = Targets.jvm.jvm,
                    excludedTargets = listOf(Targets.jvm.android),
                    intermediateSourceSets = listOf(SourceSets.allJvm)
            ),
            CPFix(
                    name = "nativeHost",
                    mainTarget = Targets.Native.host,
                    excludedTargets = Targets.Native.all - Targets.Native.host,
                    intermediateSourceSets = listOf(SourceSets.allNative, SourceSets.allPosix),
                    excludedSourceSets = listOf(SourceSets.allIos)
            ),
            CPFix(
                    name = "ios",
                    mainTarget = Targets.Native.iosX64,
                    excludedTargets = Targets.Native.all - Targets.Native.iosX64,
                    intermediateSourceSets = listOf(SourceSets.allNative, SourceSets.allPosix, SourceSets.allDarwin, SourceSets.allIos)
            )
    )

    val appliedCpFixes = (project.findProperty("classpathFixes") as String?)
            ?.split(",")
            ?.map { it.trim() }
            ?.also { if ("host" in it && "ios" in it) error("You cannot apply both host and ios classpath fixes at the same time") }
            ?: emptyList()

    val cpFixes = appliedCpFixes
            .map { name -> availableCpFixes.find { it.name == name } ?: error("Unknown classpath fix: $name") }
            .toMutableList()

    fun MutableList<CPFix>.update(name: String, update: (CPFix) -> CPFix) {
        val fix = find { it.name == name } ?: return
        remove(fix)
        add(update(fix))
    }

    val excludedTargets = (
            (project.findProperty("excludeTargets") as String?)
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.flatMap {
                        when (it) {
                            "native" -> Targets.Native.all
                            "jvm" -> Targets.JVM.all
                            "js" -> Targets.JS.all
                            "nativeNonHost" -> Targets.Native.all - Targets.Native.host
                            else -> listOf(Targets[it])
                        }
                    }
                    ?: emptyList()
            ).plus(cpFixes.flatMap { it.excludedTargets })

    internal var crossTargets = ArrayList<String>()
    internal var hostTargets = ArrayList<String>()
    internal var universalTargets = ArrayList<String>()

    fun NamedDomainObjectContainer<out KotlinSourceSet>.add(sourceSet: KodeinSourceSet): String? {
        for (fix in cpFixes) {
            if (sourceSet in fix.excludedSourceSets) return null
        }

        val name = cpFixes.find { sourceSet in it.intermediateSourceSets } ?.mainTarget?.name ?: sourceSet.name

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
        if (excludedTargets.any { it.target == target.target }) {
            project.logger.warn("Target ${target.name} excluded.")
            return
        }

        val ktTarget = targets.findByName(target.name) ?: run {
            @Suppress("UNCHECKED_CAST")
            val preset = (presets.findByName(target.target) ?: throw IllegalArgumentException("Unknown target ${target.name}")) as KotlinTargetPreset<T>
            val ktTarget = preset.createTarget(target.name).also { targets.add(it) }

            cpFixes.filter { it.mainTarget == target }
                    .forEach { fix ->
                        fix.intermediateSourceSets.forEach { iss ->
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
        if (excludedTargets.any { it.target == target.target }) return //TODO: remove this once IDEA correctly handles intermediate source sets.
        SourceSetBuilderImpl(sourceSets, target.name).apply(conf)
    }

    fun KotlinMultiplatformExtension.sourceSet(sourceSet: KodeinSourceSet, conf: SourceSetBuilder.() -> Unit) {
        for (fix in cpFixes) {
            if (sourceSet in fix.excludedSourceSets) return
            if (sourceSet in fix.intermediateSourceSets) {
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

    var enableCrossCompilation: Boolean = false

    fun KotlinDependencyHandler.compileOnlyAndroidJar(version: Int = 19, laterVersionAllowed: Boolean = true) {
        compileOnly(KodeinJvmExtension.androidJar(project, version, laterVersionAllowed))
    }

}