package io.github.mackimaow.kotlin.union

/**
 * Represents a union case that is an instance of the class [T] with additional requirement to satisfy
 * specified by [isCase].
 * @param CS The [UCases] which registered this case.
 * @param T The type which is concerned with this case.
 * @property isCase is a predicate that specifies if a particular instance of [T] is of this case.
 * @property typeCast is a function that casts a value to the type [T] if it is of that type.
 * @property name is the name of the case
 * @property ordinal is the position of the case in the union
 * @property parent is the [UCases] instance that contains this instance
 */
class SpecificInstanceCase<CS: UCases<CS>, T: Any> internal constructor(
    override val isCase: (T) -> Boolean,
    override val typeCast: (Any?) -> T?,
    override val name: String,
    override val ordinal: Int,
    override val parent: CS
): UCase<CS, T>() {

    /**
     * Wraps the specified object as a [Union] if it is the specific case.
     * @param obj the object to wrap
     * @return the [Union] wrapping the object (if it is the specific case)
     */
    fun wrap(obj: T): Union<CS>? {
        return if (isCase(obj))
            wrapUnion<CS>(obj)
        else
            null
    }

    override fun toString(): String {
        return "${parent::class.simpleName}.${this::class.simpleName}(name=$name, ordinal=$ordinal)"
    }
}

/**
 * Wraps [this] object as a [Union] with the specified [case].
 * @param CS [UCases] specifying the union cases for the [Union]
 * @param T the type of the object to wrap
 * @param case the [SpecificInstanceCase] that this object should be represented as within the union.
 * @return [this] as a [Union] (if it is the specific case) otherwise null
 */
fun <CS: UCases<CS>, T: Any> T.wrapAs(case: SpecificInstanceCase<CS, T>): Union<CS>? {
    return case.wrap(this)
}