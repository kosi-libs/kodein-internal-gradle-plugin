package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit

@Suppress("UNUSED_TYPEALIAS_PARAMETER")
typealias KodeinJvmTarget = KodeinMPPExtension.KodeinTarget<KotlinJvmTarget>
typealias KodeinJsTarget = KodeinMPPExtension.KodeinTarget<KotlinJsTarget>
typealias KodeinNativeTarget = KodeinMPPExtension.KodeinTarget<KotlinNativeTarget>
typealias KodeinAndroidTarget = KodeinMPPExtension.KodeinTarget<KotlinAndroidTarget>

private val os = OperatingSystem.current()

@Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage")
class KodeinMPPExtension(val project: Project) {

    data class KodeinSourceSet internal constructor(
            val name: String,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val mainConf: SourceSetConf = {},
            val testConf: SourceSetConf = {}
    )

    object SourceSets {
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
                name = "allNative",
                mainConf = { dependsOn(it.getByName("commonMain")) },
                testConf = { dependsOn(it.getByName("commonTest")) }
        )

        val allPosix = KodeinSourceSet(
                name = "allPosix",
                dependencies = listOf(allNative)
        )

        val allIos = KodeinSourceSet(
                name = "allIos",
                dependencies = listOf(allPosix)
        )
    }

    val kodeinSourceSets = SourceSets

    data class KodeinTarget<T : KotlinTarget> internal constructor(
            val target: String,
            val name: String = target,
            val dependencies: List<KodeinSourceSet> = emptyList(),
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
                        target.publishLibraryVariants("release")
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
            val androidArm32 = KodeinNativeTarget(
                    target = "androidNativeArm32",
                    name = "androidArm32",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val androidArm64 = KodeinNativeTarget(
                    target = "androidNativeArm64",
                    name = "androidArm64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val iosArm32 = KodeinNativeTarget(
                    target = "iosArm32",
                    dependencies = listOf(SourceSets.allIos)
            )

            val iosArm64 = KodeinNativeTarget(
                    target = "iosArm64",
                    dependencies = listOf(SourceSets.allIos)
            )

            val iosX64 = KodeinNativeTarget(
                    target = "iosX64",
                    dependencies = listOf(SourceSets.allIos)
            )

            val tvosArm64 = KodeinNativeTarget(
                    target = "tvosArm64",
                    name = "tvosArm64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val tvosX64 = KodeinNativeTarget(
                    target = "tvosX64",
                    name = "tvosX64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val watchosArm32 = KodeinNativeTarget(
                    target = "watchosArm32",
                    name = "watchosArm32",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val watchosArm64 = KodeinNativeTarget(
                    target = "watchosArm64",
                    name = "watchosArm64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val watchosX86 = KodeinNativeTarget(
                    target = "watchosX86",
                    name = "watchosX86",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxArm32Hfp = KodeinNativeTarget(
                    target = "linuxArm32Hfp",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxMips32 = KodeinNativeTarget(
                    target = "linuxMips32",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxMipsel32 = KodeinNativeTarget(
                    target = "linuxMipsel32",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val linuxX64 = KodeinNativeTarget(
                    target = "linuxX64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val macosX64 = KodeinNativeTarget(
                    target = "macosX64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val mingwX64 = KodeinNativeTarget(
                    target = "mingwX64",
                    dependencies = listOf(SourceSets.allPosix)
            )

            val wasm32 = KodeinNativeTarget(
                    target = "wasm32",
                    dependencies = listOf(SourceSets.allNative)
            )

            val allAndroid = listOf(androidArm32, androidArm64)
            val allIos = listOf(iosArm32, iosArm64, iosX64)
            val allWatchos = listOf(watchosArm32, watchosArm64, watchosX86)
            val allTvos = listOf(tvosArm64, tvosX64)
            val allMobile = allAndroid + allIos

            val allDesktop = listOf(linuxX64, macosX64, mingwX64)

            val allLinux = listOf(linuxArm32Hfp, linuxMips32, linuxMipsel32, linuxX64)
            val allPosix = allLinux + macosX64 + mingwX64 + allMobile

            val all = allPosix + wasm32

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
        operator fun get(name: String) = all.firstOrNull { it.target == name } ?: error("No Kodein target named $name")
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
                    intermediateSourceSets = listOf(SourceSets.allNative, SourceSets.allPosix, SourceSets.allIos)
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

    val excludedTargets = (
            (project.findProperty("excludeTargets") as String?)
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.flatMap {
                        when (it) {
                            "nativeNonHost" -> Targets.Native.all - Targets.Native.host
                            else -> listOf(Targets[it])
                        }
                    }
                    ?: emptyList()
            ).plus(cpFixes.flatMap { it.excludedTargets })


    fun NamedDomainObjectContainer<out KotlinSourceSet>.add(sourceSet: KodeinSourceSet): String? {
        for (fix in cpFixes) {
            if (sourceSet in fix.excludedSourceSets) return null
        }

        val name = cpFixes.find { sourceSet in it.intermediateSourceSets } ?.mainTarget?.name ?: sourceSet.name

        val main = maybeCreate(name + "Main")
        val test = maybeCreate(name + "Test")

        sourceSet.mainConf(main, this)
        sourceSet.testConf(test, this)

        sourceSet.dependencies.forEach { dep ->
            add(dep)?.takeIf { it != name } ?.let {
                main.dependsOn(getByName(it + "Main"))
                test.dependsOn(getByName(it + "Test"))
            }
        }

        return name
    }


    interface SourceSetBuilder {
        val main: KotlinSourceSet
        val test: KotlinSourceSet
        fun main(block: KotlinSourceSet.() -> Unit) { main.apply(block) }
        fun test(block: KotlinSourceSet.() -> Unit) { test.apply(block) }
        fun dependsBothOn(name: String)
    }

    private class SourceSetBuilderImpl(val sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, name: String) : SourceSetBuilder {
        override val main: KotlinSourceSet = sourceSets.maybeCreate(name + "Main")
        override val test: KotlinSourceSet = sourceSets.maybeCreate(name + "Test")
        override fun dependsBothOn(name: String) {
            main.dependsOn(sourceSets[name + "Main"])
            test.dependsOn(sourceSets[name + "Test"])
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

    private class TargetBuilderImpl<T : KotlinTarget> private constructor(val ssb: SourceSetBuilder, val tc: TargetConfigurator<T>) : TargetBuilder<T>, SourceSetBuilder by ssb, TargetConfigurator<T> by tc {
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
}
