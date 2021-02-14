import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}


repositories {
    mavenCentral()
}

task<Sync>("syncVersionSources") {
    from(file("../kodein-internal-gradle-versions/src/main"))
    into("$buildDir/srcMain")
}

tasks["compileKotlin"].dependsOn("syncVersionSources")

sourceSets {
    main {
        val kotlinSourceSet = (this as HasConvention).convention.getPlugin(KotlinSourceSet::class.java)

        kotlinSourceSet.kotlin.srcDir(file("$buildDir/srcMain/kotlin"))
        kotlinSourceSet.resources.srcDir(file("$buildDir/srcMain/resources"))
    }
}
