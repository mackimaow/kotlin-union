package io.github.mackimaow.kotlin.union


/**
 * Represents a registered case within a [Union]. This is created by instantiating an
 * instance of [UCases] by using the protected methods [UCases.obj], [UCases.instance], [UCases.instanceWhen],
 * [UCases.union], or [UCases.unionWhen] to construct instances of [UCase].
 *
 * @param CS The parent [UCases] type which this case is registered under.
 * @param T The object type associated with this case.
 * @property isCase is a predicate that specifies if a particular instance is this case.
 * @property typeCast is a function that casts a value to the type [T] if it is of that type, otherwise None.
 * @property name is the name of the case
 * @property ordinal is the position of the case in the union
 * @property parent is the [UCases] instance that registered this case
 * @see UCases
 * @see Union
 */
sealed class UCase<CS: UCases<CS>, T> {
    internal abstract val isCase: (T) -> Boolean
    internal abstract val typeCast: (Any?) -> Optional<T>
    abstract val name: String
    abstract val ordinal: Int
    abstract val parent: CS

    internal fun getValueIfMatchesCase(
        value: Any?,
    ): Optional<T> = typeCast(value).letSome {
        return if (isCase(it))
             Optional.Some(it)
        else
            Optional.None
    }



    /**
     * Unwraps the value of the Union to the type [T] if the value matches this case. If the value is a nested union,
     * it will be unwrapped recursively.
     * @param value The union value to unwrap.
     * @return An optional with the unwrapped value if the value is this case, otherwise None.
     * @see Optional
     */
    fun unwrap(value: Union<CS>): Optional<T> {
        val unionValue = unwrapIfUnion(value)
        return typeCast(unionValue).takeIfSome { isCase(this) }
    }
}

/**
 * Unwraps the value of the Union to the type [T] if the value matches the case [case].
 * @param CS The [UCases] indicating the enumeration of cases of the union.
 * @param T The type of value wrapped in the Union.
 * @param case The case to match the value against.
 * @return An [Optional.Some] of the unwrapped value if the value matches [case], otherwise [Optional.None].
 * @see Optional
 */
fun <CS: UCases<CS>, T> Union<CS>.unwrapFrom(case: UCase<CS, T>) = case.unwrap(this)