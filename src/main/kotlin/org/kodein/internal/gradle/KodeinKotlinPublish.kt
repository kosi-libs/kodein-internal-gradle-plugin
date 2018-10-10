//package org.kodein.internal.gradle
//
//import com.github.salomonbrys.gradle.kmp.allsourcesjar.AllSourcesJarPlugin
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.gradle.api.publish.PublishingExtension
//import org.gradle.api.publish.maven.MavenPublication
//import org.gradle.kotlin.dsl.get
//import org.gradle.kotlin.dsl.invoke
//import org.gradle.kotlin.dsl.plugin
//
//class KodeinKotlinPublish : Plugin<Project> {
//
//    private fun Project.applyPlugin() {
//        apply {
//            plugin("maven-publish")
//            plugin<AllSourcesJarPlugin>()
//        }
//
//    }
//
//    override fun apply(project: Project) = project.applyPlugin()
//}
