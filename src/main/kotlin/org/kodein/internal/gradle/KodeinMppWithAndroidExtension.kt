package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation

public typealias KodeinAndroidTarget = KodeinMppExtension.Target<KotlinAndroidTarget, KotlinJvmAndroidCompilation, KodeinMppWithAndroidExtension.AndroidSources>
public typealias KodeinAndroidTargetBuilder = KodeinMppExtension.TargetBuilder<KotlinAndroidTarget, KotlinJvmAndroidCompilation, KodeinMppWithAndroidExtension.AndroidSources>

public class KodeinMppWithAndroidExtension(project: Project, kotlin: KotlinMultiplatformExtension) : KodeinMppExtension(project, kotlin) {

    public open inner class AndroidSources(name: String) : KodeinMppExtension.Sources(name) {
        override val test: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "UnitTest")
        public val instrumentedTest: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "InstrumentedTest")
    }

    public inner class Targets : KodeinMppExtension.Targets() {
        public val android: KodeinAndroidTarget = Target("android", KotlinMultiplatformExtension::androidTarget, ::AndroidSources) {
            androidJvmConfig()

            target.publishLibraryVariants("debug", "release")
            sources.testDependencies {
                implementation("androidx.test.ext:junit:1.1.1")
                implementation("androidx.test.espresso:espresso-core:3.2.0")
            }
        }

        override val all: List<KodeinTarget> get() = super.all + android
        override val allComposeUi: List<KodeinTarget> get() = super.allComposeUi + android
        override val allTestable: List<KodeinTarget> get() = super.allTestable + android
    }

    override val targets: Targets = Targets()

    public fun android(configure: KodeinAndroidTargetBuilder.() -> Unit = {}): Unit = add(targets.android, configure)

}
