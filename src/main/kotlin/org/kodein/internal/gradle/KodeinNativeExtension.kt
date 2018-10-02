package org.kodein.internal.gradle

class KodeinNativeExtension {
    val allTargets = listOf(
            "linux_x64", "linux_arm32_hfp", "linux_mips32", "linux_mipsel32",
            "macos_x64",
            "mingw_x64",
            "android_arm32", "android_arm64",
            "ios_arm32", "ios_arm64", "ios_x64",
            "wasm32"
    )
}
