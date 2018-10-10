//package org.kodein.internal.gradle
//
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.gradle.kotlin.dsl.get
//import org.gradle.kotlin.dsl.plugin
//
//class KodeinPlatformNative : Plugin<Project> {
//
//    private fun Project.applyPlugin() {
//        apply {
//            plugin("kotlin-platform-native")
//            plugin("maven-publish")
//            plugin<KodeinPublicationUpload>()
//            plugin<KodeinVersionsPlugin>()
//        }
//
//        extensions.add("kodeinNative", KodeinNativeExtension())
//
//        extensions.configure<KodeinPublicationExtension>("kodeinPublication") {
//            publications = { (project.extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension).publications.toTypedArray() }
//        }
//
//        tasks["test"].group = "verification"
//    }
//
//    override fun apply(project: Project) = project.applyPlugin()
//
//}
