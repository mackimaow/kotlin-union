package io.github.mackimaow.kotlin.union

/**
 * This provides a base interface for any [Union] type. When using KotlinJS,
 * one may use this as a base class for external declarations for [Union] types.
 *
 * - In KotlinJS, all elements in the union are cast dynamically to [Union].
 * - In KotlinJVM, all elements in the union are wrapped by inline value class that inherits [Union].
 * - In KotlinNative, all elements in the union are wrapped by inline value class that inherits [Union].
 *
 * @param L The list of types (called UnionOptions) in the union.
 * @see UnionOptions
 */
expect sealed interface Union<L: UnionOptions<L>>

internal expect fun <L: UnionOptions<L>> wrapUnion(obj: Any): Union<L>
expect fun <L: UnionOptions<L>> unwrapToAny(obj: Union<L>): Any


