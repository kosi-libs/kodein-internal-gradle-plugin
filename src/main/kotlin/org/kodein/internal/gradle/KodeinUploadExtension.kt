package org.kodein.internal.gradle

interface KodeinUploadExtension {
    var name: String
    var description: String
}

class KodeinBintrayUploadExtension : KodeinUploadExtension {

    override var name: String = ""

    override var description: String = ""

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
