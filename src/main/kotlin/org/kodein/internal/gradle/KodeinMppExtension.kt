package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

public typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit

public typealias KodeinJvmTarget = KodeinMppExtension.KodeinTarget<KotlinJvmTarget>
public typealias KodeinJsTarget = KodeinMppExtension.KodeinTarget<KotlinJsIrTarget>
public typealias KodeinNativeTarget = KodeinMppExtension.KodeinTarget<KotlinNativeTarget>
public typealias KodeinAndroidTarget = KodeinMppExtension.KodeinTarget<KotlinAndroidTarget>

private val os = OperatingSystem.current()

@Suppress("MemberVisibilityCanBePrivate", "unused")
public class KodeinMppExtension(private val project: Project) {

    public data class KodeinSourceSet internal constructor(
        val name: String,
        val dependencies: List<KodeinSourceSet> = emptyList(),
        val mainConf: SourceSetConf = {},
        val testConf: SourceSetConf = {}
    )

    public object SourceSets {
        public fun new(name: String, dependencies: List<KodeinSourceSet> = emptyList(), mainConf: SourceSetConf = {}, testConf: SourceSetConf = {}): KodeinSourceSet =
                KodeinSourceSet(name, dependencies, mainConf, testConf)

        public val allJvm: KodeinSourceSet = KodeinSourceSet(
            name = "allJvm",
            testConf = {
                dependencies {
                    if (project.properties["org.kodein.no-default-junit"] != "true") {
                        api("org.jetbrains.kotlin:kotlin-test-junit")
                    }
                }
            }
        )

        public val allJs: KodeinSourceSet = KodeinSourceSet(
            name = "allJs",
            testConf = {
                dependencies {
                    api("org.jetbrains.kotlin:kotlin-test-js")
                }
            }
        )

        public val allNative: KodeinSourceSet = KodeinSourceSet(
            name = "allNative"
        )

        public val allPosix: KodeinSourceSet = KodeinSourceSet(
            name = "allPosix",
            dependencies = listOf(allNative)
        )

        public val allDarwin: KodeinSourceSet = KodeinSourceSet(
            name = "allDarwin",
            dependencies = listOf(allPosix)
        )

        public val allIos: KodeinSourceSet = KodeinSourceSet(
            name = "allIos",
            dependencies = listOf(allDarwin)
        )
    }

    public val kodeinSourceSets: SourceSets = SourceSets

    public data class KodeinTarget<T : KotlinTarget> internal constructor(
        val name: String,
        val preset: String = name,
        val dependencies: MutableList<KodeinSourceSet> = ArrayList(),
        val nativeBuildOn: OperatingSystem.() -> Boolean? = { null },
        val conf: TargetBuilder<T>.() -> Unit = {}
    ) {
        public operator fun invoke(name: String): KodeinTarget<T> = copy(name = name)
    }

    public object Targets {

        public object JVM {
            public val jvm: KodeinJvmTarget = KodeinJvmTarget(
                name = "jvm",
                dependencies = arrayListOf(SourceSets.allJvm),
                conf = {
                    target.compilations.all {
                        compilerOptions.configure {
                            jvmTarget.set(KodeinJvmPlugin.jvmTarget(project))
                        }
                    }
                }
            )

            public val android: KodeinAndroidTarget = KodeinAndroidTarget(
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

            public val all: List<KodeinTarget<out KotlinTarget>> = listOf(jvm, android)
        }

        public val jvm: JVM = JVM

        public object Native {
            public val iosArm32: KodeinNativeTarget = KodeinNativeTarget(
                name = "iosArm32",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            public val iosArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "iosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            public val iosSimulatorArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "iosSimulatorArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            public val iosX64: KodeinNativeTarget = KodeinNativeTarget(
                name = "iosX64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allIos)
            )

            public val tvosArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "tvosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val tvosSimulatorArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "tvosSimulatorArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val tvosX64: KodeinNativeTarget = KodeinNativeTarget(
                name = "tvosX64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val watchosArm32: KodeinNativeTarget = KodeinNativeTarget(
                name = "watchosArm32",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val watchosArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "watchosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val watchosSimulatorArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "watchosSimulatorArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val watchosX86: KodeinNativeTarget = KodeinNativeTarget(
                name = "watchosX86",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allDarwin)
            )

            public val linuxArm32Hfp: KodeinNativeTarget = KodeinNativeTarget(
                name = "linuxArm32Hfp",
                nativeBuildOn = { isLinux },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            public val linuxArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "linuxArm64",
                nativeBuildOn = { isLinux },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            public val linuxX64: KodeinNativeTarget = KodeinNativeTarget(
                name = "linuxX64",
                nativeBuildOn = { isLinux },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            public val macosX64: KodeinNativeTarget = KodeinNativeTarget(
                name = "macosX64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            public val macosArm64: KodeinNativeTarget = KodeinNativeTarget(
                name = "macosArm64",
                nativeBuildOn = { isMacOsX },
                dependencies = arrayListOf(SourceSets.allPosix)
            )

            public val mingwX64: KodeinNativeTarget = KodeinNativeTarget(
                name = "mingwX64",
                nativeBuildOn = { isWindows },
                dependencies = arrayListOf(SourceSets.allNative)
            )

            public val allIos: List<KodeinNativeTarget> = listOf(iosArm32, iosArm64, iosX64, iosSimulatorArm64)
            public val allWatchos: List<KodeinNativeTarget> = listOf(watchosArm32, watchosArm64, watchosX86, watchosSimulatorArm64)
            public val allTvos: List<KodeinNativeTarget> = listOf(tvosArm64, tvosX64, tvosSimulatorArm64)
            public val allDarwin: List<KodeinNativeTarget> = allIos + allWatchos + allTvos

            public val allDesktop: List<KodeinNativeTarget> = listOf(linuxX64, macosX64, macosArm64, mingwX64)

            public val allEmbeddedLinux: List<KodeinNativeTarget> = listOf(linuxArm32Hfp, linuxArm64)
            public val allLinux: List<KodeinNativeTarget> = allEmbeddedLinux + linuxX64

            public val allPosix: List<KodeinNativeTarget> = listOf(linuxX64, macosX64, macosArm64) + allEmbeddedLinux + allDarwin

            public val all: List<KodeinNativeTarget> = allPosix + mingwX64

            public val host: KodeinNativeTarget = when {
                os.isLinux -> linuxX64
                os.isMacOsX -> macosX64
                os.isWindows -> mingwX64
                else -> throw IllegalStateException("Unsupported OS host $os")
            }
        }

        public val native: Native = Native

        public object JS {
            public val js: KodeinJsTarget = KodeinJsTarget(
                name = "js",
                preset = "jsIr",
                dependencies = arrayListOf(SourceSets.allJs),
                conf = { target.browser() ; target.nodejs() }
            )

            public val webjs: KodeinJsTarget = KodeinJsTarget(
                name = "webjs",
                preset = "jsIr",
                dependencies = arrayListOf(SourceSets.allJs),
                conf = { target.browser() }
            )

            public val nodejs: KodeinJsTarget = KodeinJsTarget(
                name = "nodejs",
                preset = "jsIr",
                dependencies = arrayListOf(SourceSets.allJs),
                conf = { target.nodejs() }
            )

            public val all: List<KodeinJsTarget> = listOf(js, nodejs, webjs)
        }

        public val js: JS = JS

        public val all: List<KodeinTarget<out KotlinTarget>> = JVM.all + Native.all + JS.all
        public operator fun get(name: String): KodeinTarget<out KotlinTarget> = all.firstOrNull { it.name == name } ?: error("No Kodein target named $name\n Known targets are: ${all.map { it.name }.joinToString()}.")
    }

    public val kodeinTargets: Targets = Targets


    public fun KodeinSourceSet.recursiveDependencies(): List<KodeinSourceSet> = dependencies.flatMap { it.recursiveDependencies() } + this

    internal val excludedTargets = (
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
        )

    internal var crossTargets = ArrayList<String>()
    internal var hostTargets = ArrayList<String>()
    internal var universalTargets = ArrayList<String>()

    public fun NamedDomainObjectContainer<out KotlinSourceSet>.add(sourceSet: KodeinSourceSet) {
        val main = maybeCreate(sourceSet.name + "Main")
        val test = maybeCreate(sourceSet.name + "Test")

        sourceSet.mainConf(main, this)
        sourceSet.testConf(test, this)

        if (sourceSet.dependencies.isEmpty()) {
            main.dependsOn(getByName("commonMain"))
            test.dependsOn(getByName("commonTest"))
        } else {
            sourceSet.dependencies.forEach { dep ->
                add(dep)
                main.dependsOn(getByName(dep.name + "Main"))
                test.dependsOn(getByName(dep.name + "Test"))

            }
        }
    }


    public interface SourceSetBuilder {
        public val main: KotlinSourceSet
        public val test: KotlinSourceSet
        public fun main(block: KotlinSourceSet.() -> Unit) { main.apply(block) }
        public fun test(block: KotlinSourceSet.() -> Unit) { test.apply(block) }
        public fun dependsOn(sourceSet: KodeinSourceSet)
    }

    private inner class SourceSetBuilderImpl(val sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, name: String) : SourceSetBuilder {
        override val main: KotlinSourceSet = sourceSets.maybeCreate(name + "Main")
        override val test: KotlinSourceSet = sourceSets.maybeCreate(name + "Test")
        override fun dependsOn(sourceSet: KodeinSourceSet) {
            sourceSets.add(sourceSet)
            main.dependsOn(sourceSets[sourceSet.name + "Main"])
            test.dependsOn(sourceSets[sourceSet.name + "Test"])
        }
    }

    public interface TargetConfigurator<T : KotlinTarget> {
        public val mainCommonCompilation: KotlinCompilation<KotlinCommonOptions>
        public val testCommonCompilation: KotlinCompilation<KotlinCommonOptions>
        public val target: T
        public fun target(block: T.() -> Unit) { target.block() }
    }

    @Suppress("UNCHECKED_CAST")
    public val <C : KotlinCompilation<*>, T : KotlinOnlyTarget<C>> TargetConfigurator<T>.mainCompilation: C
        get() = mainCommonCompilation as C
    @Suppress("UNCHECKED_CAST")
    public val <C : KotlinCompilation<*>, T : KotlinOnlyTarget<C>> TargetConfigurator<T>.testCompilation: C
        get() = testCommonCompilation as C

    private class TargetConfiguratorImpl<T : KotlinTarget>(override val target: T) : TargetConfigurator<T> {
        override val mainCommonCompilation get() = target.compilations["main"]!!
        override val testCommonCompilation get() = target.compilations["test"]!!
    }

    public interface TargetBuilder<T : KotlinTarget> : SourceSetBuilder, TargetConfigurator<T>

    private inner class TargetBuilderImpl<T : KotlinTarget> private constructor(val ssb: SourceSetBuilder, val tc: TargetConfigurator<T>) : TargetBuilder<T>, SourceSetBuilder by ssb, TargetConfigurator<T> by tc {
        constructor(sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, target: T, sourceSetName: String = target.name) : this(SourceSetBuilderImpl(sourceSets, sourceSetName), TargetConfiguratorImpl(target))
    }

    public fun <T : KotlinTarget> KotlinMultiplatformExtension.add(target: KodeinTarget<T>, conf: TargetBuilder<T>.() -> Unit = {}) {
        if (excludedTargets.any { it.name == target.name }) {
            project.logger.warn("Target ${target.name} excluded.")
            return
        }

        val ktTarget = targets.findByName(target.name) ?: run {
            @Suppress("UNCHECKED_CAST")
            val ktPreset = (presets.findByName(target.preset) ?: throw IllegalArgumentException("Unknown preset ${target.preset}")) as KotlinTargetPreset<T>
            val ktTarget = ktPreset.createTarget(target.name).also { targets.add(it) }

            target.dependencies.forEach {
                sourceSets.add(it)
                sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.getByName(it.name + "Main"))
                sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.getByName(it.name + "Test"))
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

    public fun <T : KotlinTarget> KotlinMultiplatformExtension.add(targets: Iterable<KodeinTarget<T>>, conf: TargetBuilder<T>.() -> Unit = {}) {
        targets.forEach { add(it, conf) }
    }

    public fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget<*>, conf: SourceSetBuilder.() -> Unit) {
        if (excludedTargets.any { it.name == target.name }) return //TODO: remove this once IDEA correctly handles intermediate source sets.
        SourceSetBuilderImpl(sourceSets, target.name).apply(conf)
    }

    public fun KotlinMultiplatformExtension.sourceSet(sourceSet: KodeinSourceSet, conf: SourceSetBuilder.() -> Unit) {
        sourceSets.add(sourceSet)
        SourceSetBuilderImpl(sourceSets, sourceSet.name).conf()
    }

    public fun KotlinMultiplatformExtension.sourceSet(name: String, conf: SourceSetBuilder.() -> Unit) {
        SourceSetBuilderImpl(sourceSets, name).conf()
    }

    public fun KotlinMultiplatformExtension.common(conf: TargetBuilder<KotlinOnlyTarget<KotlinCommonCompilation>>.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        TargetBuilderImpl(sourceSets, targets["metadata"] as KotlinOnlyTarget<KotlinCommonCompilation>, "common").apply(conf)
    }

    public val KotlinMultiplatformExtension.common: SourceSetBuilder
        get() = SourceSetBuilderImpl(sourceSets, "common")

    public fun KotlinTarget.setCompileClasspath() {
        val targetName = name
        project.configurations.create("compileClasspath") {
            extendsFrom(project.configurations.getByName(targetName + "CompileClasspath"))
        }
    }

    public fun KotlinMultiplatformExtension.allTargets(conf: TargetConfigurator<out KotlinTarget>.() -> Unit) {
        targets.forEach {
            if (it !is KotlinOnlyTarget<*>) return@forEach
            TargetConfiguratorImpl(it).conf()
        }
    }
}
