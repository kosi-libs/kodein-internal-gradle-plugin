package org.kodein.internal.gradle

import org.gradle.api.NamedDomainObjectCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

fun <T> NamedDomainObjectCollection<T>.configureAll(names: Iterable<String>, configureAction: T.() -> Unit) =
        names.map { getByName(it) } .forEach(configureAction)

fun KotlinMultiplatformExtension.add(target: String, name: String = target, conf: KotlinTarget.() -> Unit = {}) =
        targets.add(presets.findByName(name)!!.createTarget(name).apply(conf))

fun KotlinMultiplatformExtension.addAll(names: Iterable<String>, conf: KotlinTarget.() -> Unit = {}) = names.forEach { add(it, it, conf) }
