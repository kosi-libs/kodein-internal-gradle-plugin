package org.kodein.internal.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

public typealias KodeinAndroidTarget = KodeinMppExtension.Target<KotlinMultiplatformAndroidLibraryTarget, KotlinMultiplatformAndroidCompilation, KodeinMppWithAndroidExtension.AndroidSources>
public typealias KodeinAndroidTargetBuilder = KodeinMppExtension.TargetBuilder<KotlinMultiplatformAndroidLibraryTarget, KotlinMultiplatformAndroidCompilation, KodeinMppWithAndroidExtension.AndroidSources>

public class KodeinMppWithAndroidExtension(project: Project, kotlin: KotlinMultiplatformExtension) : KodeinMppExtension(project, kotlin) {

    public open inner class AndroidSources(name: String) : KodeinMppExtension.Sources(name) {
        override val test: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "HostTest")
        public val instrumentedTest: NamedDomainObjectProvider<KotlinSourceSet> get() = kotlin.sourceSets.named(name + "DeviceTest")
        public fun instrumentedTest(configure: KotlinSourceSet.() -> Unit) { instrumentedTest.configure(configure) }
        public fun instrumentedTestDependencies(configure: KotlinDependencyHandler.() -> Unit) { instrumentedTest.configure { dependencies(configure) } }
    }

    public inner class Targets : KodeinMppExtension.Targets() {
        public val android: KodeinAndroidTarget = Target(
            name = "android",
            kotlinAccess = { name -> targets.getByName(name) as KotlinMultiplatformAndroidLibraryTarget },
            sourceBuilder = ::AndroidSources,
        ) {
            androidJvmConfig()
            sources.testDependencies {
                implementation("androidx.test.ext:junit:1.2.1")
                implementation("androidx.test.espresso:espresso-core:3.6.1")
            }
        }

        override val all: List<KodeinTarget> get() = super.all + android
        override val allComposeUi: List<KodeinTarget> get() = super.allComposeUi + android
        override val allTestable: List<KodeinTarget> get() = super.allTestable + android
    }

    override val targets: Targets = Targets()

    public fun android(configure: KodeinAndroidTargetBuilder.() -> Unit = {}): Unit = add(targets.android, configure)

}
