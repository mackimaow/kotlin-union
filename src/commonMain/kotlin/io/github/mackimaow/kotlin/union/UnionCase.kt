package io.github.mackimaow.kotlin.union


/**
 * Represents a registered Union case for a [UCases] object. It can
 * be created by protected method [UCases.union].
 *
 * @param CSParent The [UCases] type that this case is registered under.
 * @param CSChild The [UCases] type of the child union cases.
 * @property unionCases The cases of the child union
 * @property name The name of the case
 * @property ordinal The ordinal of the case
 * @property parent The parent union cases
 */
class UnionCase <
    CSParent: UCases<CSParent>,
    CSChild: MatchCases<CSChild>,
> internal constructor(
    val unionCases: CSChild,
    override val name: String,
    override val ordinal: Int,
    override val parent: CSParent
): UCase<CSParent, Union<CSChild>>() {
    override val isCase: (Union<CSChild>) -> Boolean = { true }
    override val typeCast: (Any?) -> Optional<Union<CSChild>> = {
        val unwrappedValue = unwrapCompletelyIfUnion(it)
        unionCases.wrap(unwrappedValue)
    }

    internal fun recursiveToWrappableItem(
        unwrappedValue: Any?,
        cachedUnionValues: MutableMap<MatchCases<*>, Optional<Union<*>>>
    ): Optional<*> {
        cachedUnionValues[unionCases]?.runSome {
            @Suppress("UNCHECKED_CAST")
            return (this as Union<CSChild>).asSome()
        }
        return unionCases.recursiveWrap(unwrappedValue, cachedUnionValues)
    }

    /**
     * @return the object [obj] wrapped as a [Union]
     */
    fun wrap(obj: Union<CSChild>): Union<CSParent> {
        return wrapUnion(obj)
    }

    override fun toString(): String {
        return "${parent::class.simpleName}.${this::class.simpleName}(name=$name, ordinal=$ordinal)"
    }
}

/**
 * Wraps the [Union] as a [Union] that is registered with the parent [UCases] object.
 *
 * @param CSParent The [UCases] indicating the enumeration of cases of the parent union.
 * @param CSChild The specific union (specified within [CSParent]) child cases.
 * @param case The case used when wrapping the object as a [Union]
 * @return the [Union] wrapped as a [Union] using the parent [UCases]
 */
fun <
    CSParent: UCases<CSParent>,
    CSChild: MatchCases<CSChild>
> Union<CSChild>.wrapAs(
    case: UnionCase<CSParent, CSChild>
): Union<CSParent> {
    return case.wrap(this)
}