package org.kodein.internal.gradle

import com.github.salomonbrys.gradle.kjs.jstests.addKotlinJSTest
import com.github.salomonbrys.gradle.kjs.jstests.mainJsCompileTask
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

typealias SourceSetConf = KotlinSourceSet.(NamedDomainObjectContainer<out KotlinSourceSet>) -> Unit
typealias TargetConf = KotlinTarget.() -> Unit

@Suppress("MemberVisibilityCanBePrivate", "unused")
class KodeinMPP {

    class KodeinSourceSet internal constructor(
            val name: String,
            val dependencies: List<KodeinMPP.KodeinSourceSet> = emptyList(),
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

        object Native {
            val all = KodeinSourceSet(
                    name = "allNative",
                    mainConf = { dependsOn(it.getByName("commonMain")) },
                    testConf = { dependsOn(it.getByName("commonTest")) }
            )

            val allDesktop = KodeinSourceSet(
                    name = "allDesktopNative",
                    dependencies = listOf(all)
            )

            val allMobile = KodeinSourceSet(
                    name = "allMobileNative",
                    dependencies = listOf(all)
            )
        }

        val native = Native
    }

    val kodeinSourceSets = SourceSets

    class KodeinTarget internal constructor(
            val target: String,
            val name: String = target,
            val dependencies: List<KodeinSourceSet> = emptyList(),
            val conf: TargetConf = {}
    )

    object Targets {

        val JsConf: KotlinTarget.() -> Unit = {
            mainJsCompileTask.kotlinOptions.apply {
                main = "noCall"
                moduleKind = "umd"
                sourceMap = true
            }
            addKotlinJSTest()
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
                    dependencies = listOf(SourceSets.Native.allMobile)
            )

            val androidArm64 = KodeinTarget(
                    target = "androidNativeArm64",
                    name = "androidArm64",
                    dependencies = listOf(SourceSets.Native.allMobile)
            )

            val iosArm32 = KodeinTarget(
                    target = "iosArm32",
                    dependencies = listOf(SourceSets.Native.allMobile)
            )

            val iosArm64 = KodeinTarget(
                    target = "iosArm64",
                    dependencies = listOf(SourceSets.Native.allMobile)
            )

            val iosX64 = KodeinTarget(
                    target = "iosX64",
                    dependencies = listOf(SourceSets.Native.allMobile)
            )

            val linuxArm32Hfp = KodeinTarget(
                    target = "linuxArm32Hfp",
                    dependencies = listOf(SourceSets.Native.allDesktop)
            )

            val linuxMips32 = KodeinTarget(
                    target = "linuxMips32",
                    dependencies = listOf(SourceSets.Native.allDesktop)
            )

            val linuxMipsel32 = KodeinTarget(
                    target = "linuxMipsel32",
                    dependencies = listOf(SourceSets.Native.allDesktop)
            )

            val linuxX64 = KodeinTarget(
                    target = "linuxX64",
                    dependencies = listOf(SourceSets.Native.allDesktop)
            )

            val macosX64 = KodeinTarget(
                    target = "macosX64",
                    dependencies = listOf(SourceSets.Native.allDesktop)
            )

            val mingwX64 = KodeinTarget(
                    target = "mingwX64",
                    dependencies = listOf(SourceSets.Native.allDesktop)
            )

            val allAndroid = listOf(androidArm32, androidArm64)
            val allIos = listOf(iosArm32, iosArm64, iosX64)
            val allMobile = allAndroid + allIos
            val allDesktop = listOf(linuxArm32Hfp, linuxMips32, linuxMipsel32, linuxX64, macosX64, mingwX64)
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

    fun KotlinMultiplatformExtension.add(target: KodeinTarget, conf: TargetBuilder.() -> Unit = {}) {
        val preset = presets.findByName(target.target) ?: throw IllegalArgumentException("Unknown target ${target.name}")
        val ktTarget = preset.createTarget(target.name).apply(target.conf)
        targets.add(ktTarget)
        target.dependencies.forEach {
            sourceSets.add(it)
            sourceSets.getByName(target.name + "Main").dependsOn(sourceSets.getByName(it.name + "Main"))
            sourceSets.getByName(target.name + "Test").dependsOn(sourceSets.getByName(it.name + "Test"))
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

