# Internal Gradle Configuration Plugins

This is a collection of gradle plugin that contain the common configuration for all Kodein Open-Source Initiative components.

This project is **NOT** by itself a Kodein Open-Source Initiative component, it is a build tool intended to be used **ONLY** by Kodein components.


## Settings

This plugins must be applied in `settings.gradle.kts`.
You therefore need to add the repository.
Here's a standard `settings.gradle.kts`:

```kotlin
buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://maven.pkg.github.com/kosi-libs/kodein-internal-gradle-plugin") {
            credentials {
                username = extra["github.username"]?.toString()
                  ?: error("Please set github.username in ~/.gradle/gradle.properties")
                password = extra["github.personalAccessToken"]?.toString()
                  ?: error("Please set github.personalAccessToken in ~/.gradle/gradle.properties") 
            }
        }
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:VERSION")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "PRJECT-NAME"

include(
    ":moduleA", 
    ":moduleB"
)
```

### Benefits

* Allows modules segmentation (for target exclusion).
* Adds all necessary repositories to project's gradle classpath.
* Synchronizes all versions of kodein plugins.
* Synchronizes version of Kotlin & KotlinX Serialization plugins.

### Target exclusion

You can exclude modules from being configured / compiled.
For example: you can exclude an Android modules if you don't have an Android SDK installation.

Excluded targets are configured via the `excludeTargets` **kodein local property**.
You can exclude multiple targets with a coma separated list:

```properties
excludeTargets = android, ktor
```

For a module to relate to a target, you need to include it in a special way in settings.gradle.kts:

```kotlin
android.include(":framework:android")
framework("ktor").include(":framework:ktor")
```


## Root

Each Kodein project must have a no-source root module with the `org.kodein.root` plugin applied.

### Benefits

* Configures standard dependencies repositories (maven local, jCenter, Google, Kotlin EAP).
* Applies the `org.kodein.upload.root` plugin.
* Allows to generate a report of all dependencies for all modules (with `./gradlew allDependencies --scan`)


## MavenCentral (OSSRH):

### Benefits

* Read Nexus Sonatype's configuration values once and cache them in root module.
* Configures sonatype (releases or snapshots) with `maven-publish` plugin (recommended way with KMP).
* Configures GPG signing for publication.
* Adds standard information to POM.
* Disables publications on cross and excluded targets (see MP module plugin).
* Creates the `hostOnlyPublish` task which publishes only locally built native targets.

### Configuration

MavenCentral publishing and configuration is automatically disabled if the configuration values are not set.

You can set the bintray configuration values:

* In your global **`~/.gradle/gradle.properties`**:
  ```properties
  org.kodein.sonatype.username = bintray-username
  org.kodein.sonatype.password = bintray-api-key
  ```
* In environment variables `SONATYPE_USERNAME` and `SONATYPE_PASSWORD`.

If the kodein local property `ossrh.dryRun` is `true`, the upload emulates the upload without actually uploading the items.
(or even through the `org.kodein.sonatype.dryRun` gradle parameter).

### Root module

Apply the `org.kodein.root` plugin (or the "bare" `org.kodein.upload.root` plugin).

### Artifact module

Apply any `org.kodein.library.*` plugin (or the "bare" `org.kodein.upload.module` plugin).

Configure the artifact upload:

```kotlin
kodeinUpload {
    name = "artifact-name"
    description = "Artifact description"
}
```


## Kodein local properties

Kodein local properties are personal properties that may change your local project configuration.

A Kodein local property `key` can be set:

* In environment variables, named `KODEIN_LOCAL_${key.toUppercase()}`.
* In a git ignored file `kodein.local.properties` at the root of the project repository with the key `key`.
* In the personal `~/.gradle/gradle.properties` file, with the key `org.kodein.local.${key}`.


## JVM module plugin

* If the module is published, apply the `org.kodein.library.jvm` plugin.
* If the module is internal to the project, apply the `org.kodein.jvm` plugin.

### Benefits

* Global:
  * Configures JVM target to 1.8.
  * Sets Kotlin progressiveMode.
  * Adds JUnit to test dependencies.
  * Configures test tasks to display in console.
* Library:
  * Creates maven artifacts (with source jar).
  * Adds `org.kodein.upload.module` plugin.
  * Sets the explicit API Kotlin option


## Android module plugin

* If the module is published, apply the `org.kodein.library.android` plugin.
* If the module is internal to the project, apply the `org.kodein.android` plugin.

### Benefits

* Global:
  * Same benefits as the JVM plugin.
  * Configures Android compile and min sdk version.
  * Adds Espresso to test dependencies.
  * Configures Android JUnit test runner.
* Library:
  * Creates AAR maven artifacts (with source jar).
  * Adds `org.kodein.upload.module` plugin and the `android-maven-publish` plugin.


## MP module plugin

* If the module is published:
  * If the module targets Android, apply the `org.kodein.library.mpp-with-android` plugin.
  * If the module does not target Android, apply the `org.kodein.library.mpp` plugin.
* If the module is internal to the project:
  * If the module targets Android, apply the `org.kodein.mpp-with-android` plugin.
  * If the module does not target Android, apply the `org.kodein.mpp` plugin.

### Benefits

* Global:
  * Adds multiple shortcut helpers to the Kotlin targets & source sets configuration DSL.
  * Adds multiple intermediate source sets depending on active targets, and ease the creation of new ones.
  * Enables same benefits for the JVM target as the JVM module plugin.
  * Configures test tasks to display in console.
  * Adds default test dependencies.
  * Excludes targets based on local properties (for example if you don't want to compile Android).
  * Disables native cross compilation (by default).
  * Enables both js compiler by default (legacy and IR).
  * Adds the `hostOnlyTest` gradle task that starts only host native tests.
* Library:
  * Adds `org.kodein.upload.module` plugin.
  * Sets the explicit API Kotlin option.

### MP Configuration

```kotlin
kodein {                            // Adds the kodein helpers
    kotlin {
        common {                    // Helper to access the common sourcesets
            main.dependencies {}
            test.dependencies {}
        }
        add(kodeinTargets.jvm.jvm)  // Adds the JVM Kodein target
        add(
            kodeinTargets.native.allDarwin +
            kodeinTargets.native.allDesktop
        ) {
          mainCompilation           // Helper to access main (or test) compilation
              .cinterops
              .create("libleveldb") {}
        }
        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {}
            test.dependencies {}
        }
    }
}
```

Note that you should only use `add(kodeinTargets.*)` to add a new target.

Available targets and sourcesets sourcesets can be found in the `KodeinMPPExtension.kt` source file.

### Android configuration

```kotlin
kodeinAndroid {                 // Adds the kodein helpers
    android {
        /* standard android configuration */
    }
}

if (kodeinAndroid.isIncluded) {
    /* Whatever gradle conf only enabled if Android is enabled */
}
```

The `kodeinAndroid` helper is needed to disable Android configuration if Android is an excluded target.

### JS compilers

By default, both compiler (legacy & IR) are enabled.
You can configure the toolchain to only use the legacy compiler in **`gradle.properties`**:

```properties
org.kodein.js.useOnlyLegacyCompiler = true
```

### Target exclusion

You can exclude targets from being configured / compiled.
For example: you can exclude the Android target if you don't have an Android SDK installation.

Excluded targets are configured via the `excludeTargets` **kodein local property**.
You can exclude:

* Regular targets (such as `android`, `iosArm32`, `js`).
* A list of targets. Available are: `all-native`, `all-jvm`, `all-js`, `nativeNonHost`.

You can exclude multiple targets with a coma separated list:

```properties
excludeTargets = nativeNonHost, android
```

### Native cross compilation

By default, native cross-compilation is disabled (each host only builds its own native libs).
You can enable it in **`gradle.properties`**:

```properties
org.kodein.native.enableCrossCompilation = true
```


## Gradle plugin module plugin

Apply the `org.kodein.gradle-plugin` plugin.

You also should apply the `kotlin-dsl` plugin as it does not come bundled.

### Benefits

* Applies all benefits provided by the "JVM module" plugin.
* Library:
  * Creates maven artifacts (with source jar).
  * Adds `org.kodein.upload.module` plugin.
  * Sets the explicit API Kotlin option
