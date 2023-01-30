package io.github.mackimaow.kotlin.union


/**
 * Represents a registered literal within a [UnionOptions] object. It can
 * be created by protected method ~~UnionOptions.literal()~~.
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param T the type of the literal
 *
 * @see UnionOptions
 */
class UnionLiteral<L: UnionOptions<L>, T>  internal constructor(
    private val literal: T
): UnionOption<L, T> {
    override val discriminator: (Any) -> Boolean = { it == literal }

    /**
     * @return the literal represented by [UnionLiteral] wrapped as a [Union] type.
     */
    fun wrap(): Union<L> {
        return wrapUnion(literal as Any)
    }
}