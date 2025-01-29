package io.github.mackimaow.kotlin.union

import kotlin.reflect.KClass

/**
 * This provides a base interface for any [Union] type. When using KotlinJS,
 * one may use this as a base class for external declarations for [Union] types.
 *
 * - In KotlinJS, all elements in the union are cast dynamically to [Union].
 * - In KotlinJVM and KotlinNative, all elements in the union are wrapped by inline value class that inherits [Union].
 *
 * @param CS The list of types (called [UCases]) in the union.
 * @see UCases
 */
expect sealed interface Union<CS: UCases<CS>>

internal expect fun <CS: UCases<CS>> wrapUnion(obj: Any?): Union<CS>

/**
 * This function is used to get the value of the union/nested-unions.
 *
 * @return The unwrapped value.
 */
fun <CS: UCases<CS>> Union<CS>.unwrap(): Any? = unwrapIfUnion(this)

internal expect fun unwrapIfUnion(obj: Any?): Any?
internal expect fun isUnionClass(classObj : KClass<*>): Boolean


