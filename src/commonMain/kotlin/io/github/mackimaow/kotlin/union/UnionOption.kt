package io.github.mackimaow.kotlin.union


/**
 * Represents a registered type within a [Union]. This is created by instantiating an
 * instance of [UnionOptions] by using the protected methods ~~UnionOptions.option~~ and
 * ~~UnionOptions.literal~~ to construct instances of [UnionOption].
 *
 * @param L The [UnionOptions] indicating the composition of options of the union.
 * @param T The specific type of the Union (specified within [L]).
 * @property discriminator is a predicate specifies if a particular instance is of type [T].
 * It is used to differentiate instances of [T] from others in the Union (specified within [L]). It
 * is usually specified for types that have generic parameters that are erased at runtime
 * @see UnionOptions
 * @see Union
 */
sealed interface UnionOption<L: UnionOptions<L>, T> {
    val discriminator: (Any) -> Boolean

    /**
     * @return the [Union] instance [value] unwrapped as it's original type, or
     * ~~null~~ if it can't.
     * This is indicated by what class or literal is registered by this [UnionOption]
     */
    fun unwrap(value: Union<L>): T? {
        val unionValue = unwrapToAny(value)
        return if (discriminator(unionValue))
            @Suppress("UNCHECKED_CAST")
            unionValue as T
        else null
    }
}