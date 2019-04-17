package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kotlin.js.jstests.node.KotlinMppJsTestsNodeExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit
typealias TargetConf<C> = KotlinOnlyTarget<C>.() -> Unit
typealias OSTest = OperatingSystem.() -> Boolean
typealias ConfTest = Project.() -> Boolean

typealias KodeinNativeTarget = KodeinMPPExtension.KodeinTarget<KotlinNativeCompilation>

private fun Project.isTrue(property: String) = findProperty(property) == "true"

@Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage")
class KodeinMPPExtension(val project: Project) {

    class KodeinSourceSet internal constructor(
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

    class KodeinTarget<C : KotlinCompilation<*>> internal constructor(
            val target: String,
            val name: String = target,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val conf: TargetConf<C> = {},
            val isNative: Boolean = false,
            val isNativeHost: OSTest = { false },
            val exclude: ConfTest = { false }
    )

    object Targets {

        val JsConf: KotlinOnlyTarget<KotlinJsCompilation>.() -> Unit = {
            val mainCompileTask = project.tasks[compilations["main"].compileKotlinTaskName] as Kotlin2JsCompile
            mainCompileTask.kotlinOptions.apply {
                main = "noCall"
                moduleKind = "umd"
                sourceMap = true
            }
            project.extensions.configure<KotlinMppJsTestsNodeExtension>("kotlinJsNodeTests") {
                thisTarget()
            }
        }

        val jvm = KodeinTarget<KotlinJvmCompilation>(
                target = "jvm",
                dependencies = listOf(SourceSets.allJvm)
        )

        val android = KodeinTarget<KotlinJvmAndroidCompilation>(
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

        val js = KodeinTarget(
                target = "js",
                dependencies = listOf(SourceSets.allJs),
                conf = JsConf
        )

        val webjs = KodeinTarget(
                target = "js",
                name = "webjs",
                dependencies = listOf(SourceSets.allJs),
                conf = JsConf
        )

        val nodejs = KodeinTarget(
                target = "js",
                name = "nodejs",
                dependencies = listOf(SourceSets.allJs),
                conf = JsConf
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


    open class SourceSetBuilder internal constructor(sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, name: String) {
        val main: KotlinSourceSet = sourceSets.getByName(name + "Main")
        val test: KotlinSourceSet = sourceSets.getByName(name + "Test")
        fun main(block: KotlinSourceSet.() -> Unit) { main.apply(block) }
        fun test(block: KotlinSourceSet.() -> Unit) { test.apply(block) }
    }

    class TargetBuilder<C : KotlinCompilation<*>> internal constructor(sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, val target: KotlinTarget) : SourceSetBuilder(sourceSets, target.name) {
        @Suppress("UNCHECKED_CAST")
        val mainCompilation get() = target.compilations["main"] as C
        @Suppress("UNCHECKED_CAST")
        val testCompilation get() = target.compilations["test"] as C
        fun target(block: KotlinTarget.() -> Unit) { target.block() }
    }

    // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
    private fun <C : KotlinCompilation<*>> KotlinMultiplatformExtension.addNativeCommonSrcDir(target: KodeinTarget<C>, dependencies: List<KodeinSourceSet>) {
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
    private fun <C : KotlinCompilation<*>> KotlinMultiplatformExtension.addJvmCommonSrcDir(target: KodeinTarget<C>, dependencies: List<KodeinSourceSet>) {
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

    fun <C : KotlinCompilation<*>> KotlinMultiplatformExtension.add(target: KodeinTarget<C>, conf: TargetBuilder<C>.() -> Unit = {}) {
        if (target.exclude(project)) {
            project.logger.warn("Target ${target.name} excluded.")
            return
        }

        val ktTarget = targets.findByName(target.name) ?: run {
            @Suppress("UNCHECKED_CAST")
            val preset = (presets.findByName(target.target) ?: throw IllegalArgumentException("Unknown target ${target.name}")) as KotlinTargetPreset<KotlinOnlyTarget<C>>
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
        TargetBuilder<C>(sourceSets, ktTarget).apply(conf)
    }

    fun <C : KotlinCompilation<*>> KotlinMultiplatformExtension.add(targets: Iterable<KodeinTarget<C>>, conf: TargetBuilder<C>.() -> Unit = {}) =
            targets.forEach { add(it, conf) }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget<*>, conf: SourceSetBuilder.() -> Unit) {
        SourceSetBuilder(sourceSets, target.name).apply(conf)
    }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget<*>) = SourceSetBuilder(sourceSets, target.name)

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
            SourceSetBuilder(sourceSets, sourceSet.name)
        }
    }

//    fun KotlinMultiplatformExtension.sourceSet(sourceSet: KodeinSourceSet) = SourceSetBuilder(sourceSets, sourceSet.name)

    fun KotlinMultiplatformExtension.common(conf: SourceSetBuilder.() -> Unit) {
        SourceSetBuilder(sourceSets, "common").apply(conf)
    }

    val KotlinMultiplatformExtension.common get() = SourceSetBuilder(sourceSets, "common")

    fun KotlinTarget.setCompileClasspath() {
        val targetName = name
        project.configurations.create("compileClasspath") {
            extendsFrom(project.configurations.getByName(targetName + "CompileClasspath"))
        }
    }

}

