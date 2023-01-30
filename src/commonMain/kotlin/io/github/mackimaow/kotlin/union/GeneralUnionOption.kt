package io.github.mackimaow.kotlin.union

/**
 * Represents a registered class within a [UnionOptions] object. It can
 * be created by protected method ~~UnionOptions.option()~~.
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param T the type of the registered class
 *
 * @see UnionOptions
 */
class GeneralUnionOption<L: UnionOptions<L>, T> internal constructor(
    override val discriminator: ((Any) -> Boolean)
): UnionOption<L, T> {

    /**
     * @return the instance [obj] wrapped as a type for this [Union], or
     * ~~null~~ if it can't.
     * This is indicated by what type is registered by this [GeneralUnionOption]
     */
    fun wrap(obj: T): Union<L> {
        return wrapUnion(obj as Any)
    }
}