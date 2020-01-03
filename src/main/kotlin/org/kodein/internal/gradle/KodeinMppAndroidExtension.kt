package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension

class KodeinMppAndroidExtension(val android: LibraryExtension?) {

    val isExcluded: Boolean get() = android == null
    val isIncluded: Boolean get() = android != null

    fun android(block: LibraryExtension.() -> Unit) {
        android?.apply(block)
    }

}
