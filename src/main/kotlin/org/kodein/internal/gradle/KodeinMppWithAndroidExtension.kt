package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation

public typealias KodeinAndroidTarget = KodeinMppExtension.Target<KotlinAndroidTarget, KotlinJvmAndroidCompilation, KotlinJvmOptions, KodeinMppWithAndroidExtension.AndroidSources>
public typealias KodeinAndroidTargetBuilder = KodeinMppExtension.TargetBuilder<KotlinAndroidTarget, KotlinJvmAndroidCompilation, KotlinJvmOptions, KodeinMppWithAndroidExtension.AndroidSources>

public class KodeinMppWithAndroidExtension(kotlin: KotlinMultiplatformExtension) : KodeinMppExtension(kotlin) {

    public open inner class AndroidSources(name: String) : KodeinMppExtension.Sources(name) {
        override val test: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "UnitTest")
        public val instrumentedTest: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "InstrumentedTest")
    }

    public inner class Targets : KodeinMppExtension.Targets() {
        public val android: KodeinAndroidTarget = Target("android", KotlinMultiplatformExtension::android, ::AndroidSources) {
            commonJvmConfig(KotlinJvmAndroidCompilation::compilerOptions)
            target.publishLibraryVariants("debug", "release")
            sources.testDependencies {
                implementation("androidx.test.ext:junit:1.1.1")
                implementation("androidx.test.espresso:espresso-core:3.2.0")
            }
        }

        override val all: List<KodeinTarget> get() = super.all + android
        override val allComposeStable: List<KodeinTarget> get() = super.allComposeStable + android
        override val allTestable: List<KodeinTarget> get() = super.allTestable + android
    }

    override val targets: Targets = Targets()

    public fun android(configure: KodeinAndroidTargetBuilder.() -> Unit = {}): Unit = add(targets.android, configure)

}
