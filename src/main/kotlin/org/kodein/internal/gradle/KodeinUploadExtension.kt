package org.kodein.internal.gradle

import com.jfrog.bintray.gradle.BintrayExtension

interface KodeinUploadExtension {
    var name: String
    var description: String
}

class KodeinBintrayUploadExtension(private val bintray: BintrayExtension) : KodeinUploadExtension {

    override var name: String
        get() = bintray.pkg.name
        set(value) { bintray.pkg.name = value }

    override var description: String
        get() = bintray.pkg.desc
        set(value) { bintray.pkg.desc = value }

}

class KodeinRootUploadExtension : KodeinUploadExtension {

    private fun ex(): Nothing = throw IllegalStateException("You must not configure this project's kodeinUpload. It is only there to trick gradle into allowing subprojects { kodeinUpload {} }")

    override var name: String
        get() = ex()
        set(_) { ex() }

    override var description: String
        get() = ex()
        set(_) { ex() }

}
