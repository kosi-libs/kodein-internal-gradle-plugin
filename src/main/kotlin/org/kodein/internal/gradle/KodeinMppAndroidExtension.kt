package org.kodein.internal.gradle

import com.android.build.gradle.LibraryExtension

public class KodeinMppAndroidExtension(private val android: LibraryExtension?) {

    public val isExcluded: Boolean get() = android == null
    public val isIncluded: Boolean get() = android != null

    public fun android(block: LibraryExtension.() -> Unit) {
        android?.apply(block)
    }

}
