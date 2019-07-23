package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit
typealias OSTest = OperatingSystem.() -> Boolean
typealias ConfTest = Project.() -> Boolean

@Suppress("UNUSED_TYPEALIAS_PARAMETER")
typealias KodeinJvmTarget = KodeinMPPExtension.KodeinTarget<KotlinJvmTarget>
typealias KodeinJsTarget = KodeinMPPExtension.KodeinTarget<KotlinJsTarget>
typealias KodeinNativeTarget = KodeinMPPExtension.KodeinTarget<KotlinNativeTarget>
typealias KodeinAndroidTarget = KodeinMPPExtension.KodeinTarget<KotlinAndroidTarget>

private fun Project.isTrue(property: String) = findProperty(property) == "true"

@Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage")
class KodeinMPPExtension(val project: Project) {

    data class KodeinSourceSet internal constructor(
            val name: String,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val mainConf: SourceSetConf = {},
            val testConf: SourceSetConf = {},
            val isNativeCommon: Boolean = false,
            val isJvmCommon: Boolean = false
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
                },
                isJvmCommon = true
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
                testConf = { dependsOn(it.getByName("commonTest")) },
                isNativeCommon = true
        )

        val allNativePosix = KodeinSourceSet(
                name = "allNativePosix",
                dependencies = listOf(allNative),
                isNativeCommon = true
        )
    }

    val kodeinSourceSets = SourceSets

    data class KodeinTarget<T : KotlinTarget> internal constructor(
            val target: String,
            val name: String = target,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val conf: T.() -> Unit = {},
            val isNative: Boolean = false,
            val isNativeHost: OSTest = { false },
            val exclude: ConfTest = { false }
    ) {
        operator fun invoke(name: String) = copy(name = name)
    }

    object Targets {

        val jvm = KodeinJvmTarget(
                target = "jvm",
                dependencies = listOf(SourceSets.allJvm),
                conf = {
                    compilations.all {
                        compileKotlinTask.sourceCompatibility = "1.8"
                        compileKotlinTask.targetCompatibility = "1.8"
                        kotlinOptions.jvmTarget = "1.8"
                    }
                }
        )

        val android = KodeinAndroidTarget(
                target = "android",
                dependencies = listOf(SourceSets.allJvm),
                exclude = { isTrue("excludeAndroid") }
        )

        object Native {
            val androidArm32 = KodeinNativeTarget(
                    target = "androidNativeArm32",
                    name = "androidArm32",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val androidArm64 = KodeinNativeTarget(
                    target = "androidNativeArm64",
                    name = "androidArm64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val iosArm32 = KodeinNativeTarget(
                    target = "iosArm32",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val iosArm64 = KodeinNativeTarget(
                    target = "iosArm64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val iosX64 = KodeinNativeTarget(
                    target = "iosX64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val linuxArm32Hfp = KodeinNativeTarget(
                    target = "linuxArm32Hfp",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val linuxMips32 = KodeinNativeTarget(
                    target = "linuxMips32",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val linuxMipsel32 = KodeinNativeTarget(
                    target = "linuxMipsel32",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val linuxX64 = KodeinNativeTarget(
                    target = "linuxX64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    isNativeHost = OperatingSystem::isLinux,
                    exclude = { isTrue("excludeNonHostNativeTargets") && !OperatingSystem.current().isLinux }

            )

            val macosX64 = KodeinNativeTarget(
                    target = "macosX64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNative = true,
                    isNativeHost = OperatingSystem::isMacOsX,
                    exclude = { isTrue("excludeNonHostNativeTargets") && !OperatingSystem.current().isMacOsX }
            )

            val mingwX64 = KodeinNativeTarget(
                    target = "mingwX64",
                    dependencies = listOf(SourceSets.allNative),
                    isNative = true,
                    isNativeHost = OperatingSystem::isWindows,
                    exclude = { isTrue("excludeNonHostNativeTargets") && !OperatingSystem.current().isWindows }
            )

            val wasm32 = KodeinNativeTarget(
                    target = "wasm32",
                    dependencies = listOf(SourceSets.allNative),
                    isNative = true,
                    exclude = { isTrue("excludeNonHostNativeTargets") }
            )

            val allAndroid = listOf(androidArm32, androidArm64)
            val allIos = listOf(iosArm32, iosArm64, iosX64)
            val allMobile = allAndroid + allIos

            val allLinux = listOf(linuxArm32Hfp, linuxMips32, linuxMipsel32, linuxX64)
            val allPosix = allLinux + allMobile + macosX64

            val allDesktop = allLinux + macosX64 + mingwX64

            val allNonWeb = allMobile + allDesktop

            val all = allNonWeb + wasm32
        }

        val native = Native

        val js = KodeinJsTarget(
                target = "js",
                dependencies = listOf(SourceSets.allJs),
                conf = { browser() ; nodejs() }
        )

        val webjs = KodeinJsTarget(
                target = "js",
                name = "webjs",
                dependencies = listOf(SourceSets.allJs),
                conf = { browser() }
        )

        val nodejs = KodeinJsTarget(
                target = "js",
                name = "nodejs",
                dependencies = listOf(SourceSets.allJs),
                conf = { nodejs() }
        )
    }

    val kodeinTargets = Targets

    fun NamedDomainObjectContainer<out KotlinSourceSet>.add(sourceSet: KodeinSourceSet) {
        sourceSet.dependencies.forEach { add(it) }

        if (findByName(sourceSet.name + "Main") == null)
            create(sourceSet.name + "Main") {
                sourceSet.mainConf(this, this@add)
                sourceSet.dependencies.forEach { dependsOn(getByName(it.name + "Main")) }
            }

        if (findByName(sourceSet.name + "Test") == null)
            create(sourceSet.name + "Test") {
                sourceSet.testConf(this, this@add)
                sourceSet.dependencies.forEach { dependsOn(getByName(it.name + "Test")) }
            }
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

    // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
    private fun <T : KotlinTarget> KotlinMultiplatformExtension.addNativeCommonSrcDir(target: KodeinTarget<T>, dependencies: List<KodeinSourceSet>) {
        dependencies.forEach {
            if (it.isNativeCommon) {
                sourceSets.getByName(target.name + "Main").kotlin.srcDir("src/${it.name}Main/kotlin")
                sourceSets.getByName(target.name + "Test").kotlin.srcDir("src/${it.name}Test/kotlin")
                it.mainConf(sourceSets.getByName(target.name + "Main"), sourceSets)
                it.testConf(sourceSets.getByName(target.name + "Test"), sourceSets)
                addNativeCommonSrcDir(target, it.dependencies)
            }
        }
    }

    // TODO: remove this fix once the KT plugin correctly identifies allJvmMain as JVM sources instead of common sources
    private fun <T : KotlinTarget> KotlinMultiplatformExtension.addJvmCommonSrcDir(target: KodeinTarget<T>, dependencies: List<KodeinSourceSet>) {
        dependencies.forEach {
            if (it.isJvmCommon) {
                sourceSets.getByName(target.name + "Main").kotlin.srcDir("src/${it.name}Main/kotlin")
                sourceSets.getByName(target.name + "Test").kotlin.srcDir("src/${it.name}Test/kotlin")
                it.mainConf(sourceSets.getByName(target.name + "Main"), sourceSets)
                it.testConf(sourceSets.getByName(target.name + "Test"), sourceSets)
                addJvmCommonSrcDir(target, it.dependencies)
            }
        }
    }

    fun <T : KotlinTarget> KotlinMultiplatformExtension.add(target: KodeinTarget<T>, conf: TargetBuilder<T>.() -> Unit = {}) {
        if (target.exclude(project)) {
            project.logger.warn("Target ${target.name} excluded.")
            return
        }

        val ktTarget = targets.findByName(target.name) ?: run {
            @Suppress("UNCHECKED_CAST")
            val preset = (presets.findByName(target.target) ?: throw IllegalArgumentException("Unknown target ${target.name}")) as KotlinTargetPreset<T>
            val ktTarget = preset.createTarget(target.name).apply(target.conf).also { targets.add(it) }

            // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
            val os = OperatingSystem.current()
            if (project.isTrue("nativeCommonHost") && target.isNativeHost(os)) {
                addNativeCommonSrcDir(target, target.dependencies)
            }

            // TODO: remove this fix once the KT plugin correctly identifies allJvmMain as JVM sources instead of common sources
            if (project.isTrue("allJvmAsJvmOnly") && target.name == "jvm") {
                addJvmCommonSrcDir(target, target.dependencies)
            }

            target.dependencies.forEach {
                // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
                if (project.isTrue("nativeCommonHost") && it.isNativeCommon) {
                    if (!target.isNativeHost(os)) {
                        val osTarget = when {
                            os.isLinux -> kodeinTargets.native.linuxX64
                            os.isMacOsX -> kodeinTargets.native.macosX64
                            os.isWindows -> kodeinTargets.native.mingwX64
                            else -> throw IllegalStateException("Unsupported OS $os, please set the nativeCommonHost property to false")
                        }
                        if (targets.findByName(osTarget.name) == null) {
                            add(osTarget)
                        }
                        sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.maybeCreate(osTarget.name + "Main"))
                        sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.maybeCreate(osTarget.name + "Test"))
                    }
                // TODO: remove this fix once the KT plugin correctly identifies allJvmMain as JVM sources instead of common sources
                } else if (project.isTrue("allJvmAsJvmOnly") && it.isJvmCommon) {
                    if (target.name != "jvm") {
                        sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.maybeCreate("jvmMain"))
                        sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.maybeCreate("jvmTest"))
                    }
                } else {
                    sourceSets.add(it)
                    sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.getByName(it.name + "Main"))
                    sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.getByName(it.name + "Test"))
                }
            }
            ktTarget
        }
        @Suppress("UNCHECKED_CAST")
        TargetBuilderImpl(sourceSets, ktTarget as T).apply(conf)
    }

    fun <T : KotlinTarget> KotlinMultiplatformExtension.add(targets: Iterable<KodeinTarget<T>>, conf: TargetBuilder<T>.() -> Unit = {}) =
            targets.forEach { add(it, conf) }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget<*>, conf: SourceSetBuilder.() -> Unit) {
        SourceSetBuilderImpl(sourceSets, target.name).apply(conf)
    }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget<*>): SourceSetBuilder = SourceSetBuilderImpl(sourceSets, target.name)

    fun KotlinMultiplatformExtension.sourceSet(sourceSet: KodeinSourceSet, conf: SourceSetBuilder.() -> Unit) {
        // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
        if (project.isTrue("nativeCommonHost") && sourceSet.isNativeCommon) {
            val os = OperatingSystem.current()
            val target = when {
                os.isLinux -> kodeinTargets.native.linuxX64
                os.isMacOsX -> kodeinTargets.native.macosX64
                os.isWindows -> kodeinTargets.native.mingwX64
                else -> throw IllegalStateException("Unsupported OS $os")
            }
            add(target, conf)
        // TODO: remove this fix once the KT plugin correctly identifies allJvmMain as JVM sources instead of common sources
        } else if (project.isTrue("allJvmAsJvmOnly") && sourceSet.isNativeCommon) {
            add(kodeinTargets.jvm, conf)
        } else {
            sourceSets.add(sourceSet)
            SourceSetBuilderImpl(sourceSets, sourceSet.name)
        }
    }

    fun KotlinMultiplatformExtension.sourceSet(name: String): SourceSetBuilder = SourceSetBuilderImpl(sourceSets, name)

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
