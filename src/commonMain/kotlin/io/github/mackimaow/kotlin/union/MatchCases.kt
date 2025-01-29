package io.github.mackimaow.kotlin.union

/**
 * A UCases type that represents a different mode of specifying cases of a union.
 * The cases specific within this class require a full match of type, and not against all cases within
 * the union like [DiscernCases] allows.
 * @param CS This class type.
 * @see MatchCases
 * @see UCases
 */
open class MatchCases<CS: MatchCases<CS>>: UCases<CS>() {
    /**
     * @return whether the instance [obj] can be wrapped as a type for a [Union] with these cases.
     * The return value is determined with matching with the cases that are registered by this [MatchCases]
     */
    fun canWrap(obj: Any?): Boolean {
        return wrap(obj).isSome
    }

    /**
     * @return the instance [obj] wrapped as [Union], or
     * [Optional.None] if it doesn't match the cases in this [MatchCases].
     */
    fun wrap(obj: Any?): Optional<Union<CS>> {
        val unwrappedValue = unwrapIfUnion(obj)
        return asWrappableItem(unwrappedValue).letSome {
            wrapUnion(it)
        }
    }

    internal fun recursiveWrap(
        unwrappedValue: Any?,
        cachedUnionValues: MutableMap<MatchCases<*>, Optional<Union<*>>>
    ): Optional<Union<CS>> {
        val item = asWrappableItem(unwrappedValue, cachedUnionValues).letSome { value ->
            wrapUnion<CS>(value)
        }
        cachedUnionValues[this] = item
        return item
    }

    private fun asWrappableItem(
        unwrappedValue: Any?,
        cachedUnionValues: MutableMap<MatchCases<*>, Optional<Union<*>>> = mutableMapOf()
    ): Optional<Any?> {
        for (case in cases) {
            val item = when (case) {
                is UnionCase<*, *> ->
                    case.recursiveToWrappableItem(unwrappedValue, cachedUnionValues)
                is SpecificUnionCase<*, *> ->
                    case.recursiveToWrappableItem(unwrappedValue, cachedUnionValues)
                else -> case.getValueIfMatchesCase(unwrappedValue)
            }
            if(item is Optional.Some)
                return item
        }
        return Optional.None
    }
}

/**
 * Wraps an instance as a type for a [Union] if it can be wrapped.
 * @param cases the [MatchCases] that this union is registered with
 * @return the instance wrapped as a [Optional.Some] of a [Union], or [Optional.None] if it doesn't match the cases
 */
fun <CS: MatchCases<CS>> Any?.wrapAs(cases: CS): Optional<Union<CS>> = cases.wrap(this)

/**
 * Checks if an instance can be wrapped as a type for a [Union].
 * @param cases the [MatchCases] that this union is registered with
 * @return whether the instance can be wrapped as a type for a [Union].given the cases [cases]
 */
fun <CS: MatchCases<CS>> Any?.canWrapAs(cases: CS): Boolean = cases.canWrap(this)