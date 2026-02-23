package io.github.mackimaow.kotlin.union

/**
 * Represents a registered Union type with a constraint within a [UCases] object. It can
 * be created by protected method [UCases.unionWhen].
 *
 * @param CSParent The [UCases] what this case is registered under.
 * @param CSChild The specific Union (specified within [CSParent]) child cases.
 * @property unionCases The cases of the union case
 * @property isCase A function that returns whether the instance [Union] is considered this case
 * @property name The name of this case
 * @property ordinal The ordinal of this case
 * @property parent The parent union this case is registered under
 */
class SpecificUnionCase<
    CSParent: UCases<CSParent>,
    CSChild: MatchCases<CSChild>,
> internal constructor(
    val unionCases: CSChild,
    override val isCase: (Union<CSChild>) -> Boolean,
    override val name: String,
    override val ordinal: Int,
    override val parent: CSParent
): UCase<CSParent, Union<CSChild>>() {
    override val typeCast: (Any?) -> Union<CSChild>? = {
        it?.run {
            val unwrappedValue = unwrapCompletelyIfUnion(this)
            unionCases.wrap(unwrappedValue)
        }
    }

    internal fun recursiveToWrappableItem(
        unwrappedValue: Any?,
        cachedUnionValues: MutableMap<MatchCases<*>, Union<*>?>
    ): Any? {
        cachedUnionValues[unionCases]?.run {
            @Suppress("UNCHECKED_CAST") // I may not need this cast here and may just use nullability
            val unionValue = (
                this as Union<CSChild>
            )
            if (isCase(unionValue))
                return unionValue
        }
        return unionCases.recursiveWrap(unwrappedValue, cachedUnionValues)?.takeIf(isCase)
    }

    /**
     * @return the instance [obj] wrapped as a [Union] if it is this case,
     * otherwise null
     */
    fun wrap(obj: Union<CSChild>): Union<CSParent>? {
        if (!isCase(obj)) return null
        return wrapUnion<CSParent>(obj)
    }

    override fun toString(): String {
        return "${parent::class.simpleName}.${this::class.simpleName}(name=$name, ordinal=$ordinal)"
    }
}

/**
 * Wraps the target [Union] as a [Union] of the parent [UCases] object.
 *
 * @param CSParent The [UCases] of the parent union.
 * @param CSChild The specific union (specified within [CSParent]) of the case.
 * @param case The specific case for which [this] should be registered under when wrapped as a union.
 * @return a [Union] wrapped within another [Union] of the parent [UCases] object
 * if it matches [case], otherwise null
 */
fun <
    CSParent: UCases<CSParent>,
    CSChild: MatchCases<CSChild>
> Union<CSChild>.wrapAs(
    case: SpecificUnionCase<CSParent, CSChild>
): Union<CSParent>? {
    return case.wrap(this)
}