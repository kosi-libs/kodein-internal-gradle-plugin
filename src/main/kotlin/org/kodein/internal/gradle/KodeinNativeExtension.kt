package org.kodein.internal.gradle

class KodeinNativeExtension {
    val allTargets = listOf(
            "macbook", "linux", "mingw",
            "android_arm32", "android_arm64",
            "iphone", "iphone_sim",
            "wasm32",
            "raspberrypi",
            "linux_mips32", "linux_mipsel32"
    )
}
