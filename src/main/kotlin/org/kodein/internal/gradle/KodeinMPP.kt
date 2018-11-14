package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kotlin.js.jstests.node.KotlinMppJsTestsNodeExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit
typealias TargetConf = KotlinTarget.() -> Unit
typealias OSTest = OperatingSystem.() -> Boolean

@Suppress("MemberVisibilityCanBePrivate", "unused", "UnstableApiUsage")
class KodeinMPP(val nativeCommonHost: Boolean) {

    class KodeinSourceSet internal constructor(
            val name: String,
            val dependencies: List<KodeinMPP.KodeinSourceSet> = emptyList(),
            val mainConf: SourceSetConf = {},
            val testConf: SourceSetConf = {},
            val isNativeCommon: Boolean = false
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

    class KodeinTarget internal constructor(
            val target: String,
            val name: String = target,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val conf: TargetConf = {},
            val isNativeHost: OSTest = { false }
    )

    object Targets {

        val JsConf: KotlinTarget.() -> Unit = {
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

        val jvm = KodeinTarget(
                target = "jvm",
                dependencies = listOf(SourceSets.allJvm)
        )

        val android = KodeinTarget(
                target = "android",
                dependencies = listOf(SourceSets.allJvm)
        )

        object Native {
            val androidArm32 = KodeinTarget(
                    target = "androidNativeArm32",
                    name = "androidArm32",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val androidArm64 = KodeinTarget(
                    target = "androidNativeArm64",
                    name = "androidArm64",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val iosArm32 = KodeinTarget(
                    target = "iosArm32",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val iosArm64 = KodeinTarget(
                    target = "iosArm64",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val iosX64 = KodeinTarget(
                    target = "iosX64",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val linuxArm32Hfp = KodeinTarget(
                    target = "linuxArm32Hfp",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val linuxMips32 = KodeinTarget(
                    target = "linuxMips32",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val linuxMipsel32 = KodeinTarget(
                    target = "linuxMipsel32",
                    dependencies = listOf(SourceSets.allNativePosix)
            )

            val linuxX64 = KodeinTarget(
                    target = "linuxX64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNativeHost = OperatingSystem::isLinux
            )

            val macosX64 = KodeinTarget(
                    target = "macosX64",
                    dependencies = listOf(SourceSets.allNativePosix),
                    isNativeHost = OperatingSystem::isMacOsX
            )

            val mingwX64 = KodeinTarget(
                    target = "mingwX64",
                    dependencies = listOf(SourceSets.allNative),
                    isNativeHost = OperatingSystem::isWindows
            )

            val allAndroid = listOf(androidArm32, androidArm64)
            val allIos = listOf(iosArm32, iosArm64, iosX64)
            val allMobile = allAndroid + allIos

            val allLinux = listOf(linuxArm32Hfp, linuxMips32, linuxMipsel32, linuxX64)
            val allPosix = allLinux + allMobile + macosX64

            val allDesktop = allLinux + macosX64 + mingwX64

            val all = allMobile + allDesktop
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

    class TargetBuilder internal constructor(sourceSets: NamedDomainObjectContainer<out KotlinSourceSet>, val target: KotlinTarget) : SourceSetBuilder(sourceSets, target.name) {
        fun target(block: KotlinTarget.() -> Unit) { target.block() }
    }

    private fun KotlinMultiplatformExtension.addSrcDir(target: KodeinTarget, dependencies: List<KodeinSourceSet>) {
        dependencies.forEach {
            if (it.isNativeCommon) {
                sourceSets.getByName(target.name + "Main").kotlin.srcDir("src/${it.name}Main/kotlin")
                sourceSets.getByName(target.name + "Test").kotlin.srcDir("src/${it.name}Test/kotlin")
                addSrcDir(target, it.dependencies)
            }
        }
    }

    fun KotlinMultiplatformExtension.add(target: KodeinTarget, conf: TargetBuilder.() -> Unit = {}) {
        val ktTarget = targets.findByName(target.name) ?: run {
            val preset = presets.findByName(target.target) ?: throw IllegalArgumentException("Unknown target ${target.name}")
            val ktTarget = preset.createTarget(target.name).apply(target.conf).also { targets.add(it) }

            // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
            val os = OperatingSystem.current()
            if (nativeCommonHost && target.isNativeHost(os)) {
                addSrcDir(target, target.dependencies)
            }

            target.dependencies.forEach {
                // TODO: remove this fix once the KT plugin correctly identifies allNativeMain as native sources instead of common sources
                if (nativeCommonHost && it.isNativeCommon) {
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
                }
                else {
                    sourceSets.add(it)
                    sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.getByName(it.name + "Main"))
                    sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.getByName(it.name + "Test"))
                }
            }
            ktTarget
        }
        TargetBuilder(sourceSets, ktTarget).apply(conf)
    }

    fun KotlinMultiplatformExtension.add(targets: Iterable<KodeinTarget>, conf: TargetBuilder.() -> Unit = {}) =
            targets.forEach { add(it, conf) }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget, conf: SourceSetBuilder.() -> Unit) {
        SourceSetBuilder(sourceSets, target.name).apply(conf)
    }

    fun KotlinMultiplatformExtension.sourceSet(target: KodeinTarget) = SourceSetBuilder(sourceSets, target.name)

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

