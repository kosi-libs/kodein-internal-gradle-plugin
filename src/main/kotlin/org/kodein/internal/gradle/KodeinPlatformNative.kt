package org.kodein.internal.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KodeinPlatformNative : Plugin<Project> {

    private fun Project.applyPlugin() {
        apply {
            plugin("konan")
//            plugin<KonanArtifactsPlugin>()
//            plugin("maven-publish")
//            plugin<KodeinPublicationUpload>()
//            plugin<KodeinVersionsPlugin>()
        }

        extensions.add("kodeinNative", KodeinNativeExtension())

//        val nat = extensions.getByName("nat") as com.github.salomonbrys.gradle.konanartifacts.NativeExtension

//        extensions.configure<KonanArtifactContainer>("konanArtifacts") {
//            library(mapOf("targets" to nat.allTargets), nat.klibArtifactName) {
//                enableMultiplatform(true)
//            }
//            testProgram(nat.klibArtifactName)
//        }

//        extensions.configure<UniversalKlibExtension>("universalKlib") {
//            packageUniversal(nat.klibArtifactName)
//        }

//        extensions.configure<PublishingExtension>("publishing") {
//            (publications) {
//                "Kodein"(MavenPublication::class) {
//                    artifact(nat.universalKlibTask(nat.klibArtifactName))
//                }
//            }
//        }
    }

    override fun apply(project: Project) = project.applyPlugin()

}
