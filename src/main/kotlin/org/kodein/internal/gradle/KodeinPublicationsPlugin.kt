package org.kodein.internal.gradle

import org.gradle.api.Project

class KodeinPublicationsPlugin : KtPlugin<Project> {

    override fun Project.applyPlugin() {
        val ext = KodeinPublicationsExtension()
        project.extensions.add("kodeinPublications", ext)
    }

}
