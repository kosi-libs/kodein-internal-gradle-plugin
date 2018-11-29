package org.kodein.internal.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

@Suppress("UnstableApiUsage")
class KodeinLibraryDependencyExtension(val project: Project) {

    infix fun ProjectDependency.target(target: String): String {
        val notation = "$group:$name-$target:$version"

        project.configurations.all {
            resolutionStrategy.dependencySubstitution {
                substitute(module(notation)).because("Kotlin multiplatform dependency fix").with(project(dependencyProject.path))
            }
        }

        return notation
    }

}
