package io.github.mackimaow.kotlin.union

/**
 * Represents a registered class within a [UCases] object. It can
 * be created by protected method [UCases.instance].
 *
 * @param CS [UCases] parent type of this case
 * @param T the type of the registered case
 * @property typeCast a function that casts a value to the type [T] if it is of that type
 * @property name the name of the registered case
 * @property ordinal the ordinal of the registered case
 * @property parent the [UCases] object that contains this registered case
 *
 * @see UCases
 */
class InstanceCase<CS: UCases<CS>, T> internal constructor(
    override val typeCast: (Any?) -> Optional<T>,
    override val name: String,
    override val ordinal: Int,
    override val parent: CS
): UCase<CS, T>() {
    override val isCase: ((T) -> Boolean) = { true }

    /**
     * @return the instance [obj] wrapped as a [Union].
     */
    fun wrap(obj: T): Union<CS> {
        return wrapUnion(obj)
    }

    override fun toString(): String {
        return "${parent::class.simpleName}.${this::class.simpleName}(name=$name, ordinal=$ordinal)"
    }
}

/**
 * Wraps [this] object as a [Union] with the specified [case].
 * @param CS [UCases] specifying the union cases for the [Union]
 * @param T the type of the object to wrap
 * @param case the [InstanceCase] to wrap the object as
 * @return the [this] object wrapped as a [Union]
 */
fun <CS: UCases<CS>, T> T.wrapAs(case: InstanceCase<CS, T>): Union<CS> {
    return case.wrap(this)
}