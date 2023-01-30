package io.github.mackimaow.kotlin.union

import kotlin.jvm.JvmInline

/**
 * This is represents the receiver for:
 * - ~~Union.map()~~ (**with** default)
 *
 *  @param L [UnionOptions] that specifies the types for the [Union] of interest
 *  @param R The return type for ~~Union.map()~~ (**with** default)
 */
@JvmInline
value class RegularResolver<L: UnionOptions<L>, R>(
    override val _state: ResolverState<L, R>,
): Resolver<L, R>