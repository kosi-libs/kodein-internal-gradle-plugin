//package org.kodein.internal.gradle
//
//import com.github.salomonbrys.gradle.kjs.jstests.JsTestsPlugin
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.gradle.kotlin.dsl.DependencyHandlerScope
//import org.gradle.kotlin.dsl.get
//import org.gradle.kotlin.dsl.plugin
//import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
//
//class KodeinPlatformJs : Plugin<Project> {
//
//    private fun Project.applyPlugin() {
//        apply {
//            plugin("kotlin-platform-js")
//            plugin<KodeinKotlinPublish>()
//            plugin<KodeinPublicationUpload>()
//            plugin<JsTestsPlugin>()
//            plugin<KodeinVersionsPlugin>()
//        }
//
//        DependencyHandlerScope(dependencies).apply {
//            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-js:${KodeinVersions.kotlin}")
//
//            "testImplementation"("org.jetbrains.kotlin:kotlin-test-js:${KodeinVersions.kotlin}")
//        }
//
//        (tasks["compileKotlin2Js"] as Kotlin2JsCompile).apply {
//            kotlinOptions.moduleKind = "umd"
//        }
//
//    }
//
//    override fun apply(project: Project) = project.applyPlugin()
//
//}
