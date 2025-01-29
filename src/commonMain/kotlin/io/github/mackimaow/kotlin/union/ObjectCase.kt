package io.github.mackimaow.kotlin.union


/**
 * Represents a registered object/literal within a [UCases] object. It can
 * be created by protected method [UCases.obj].
 *
 * @param CS the parent [UCases] that registered this case
 * @param T the type of the object
 * @property obj the object value
 * @property name the name of the case
 * @property ordinal the ordinal of the case
 * @property parent the [UCases] parent that registered this obj
 * @see UCases
 */
class ObjectCase<CS: UCases<CS>, T>  internal constructor(
    private val obj: T,
    override val name: String,
    override val ordinal: Int,
    override val parent: CS
): UCase<CS, T>() {
    override val isCase: (Any?) -> Boolean = { it == obj && obj == it }
    override val typeCast: (Any?) -> Optional<T> = {
        if (isCase(it)) obj.asSome() else Optional.None
    }

    /**
     * @return the object wrapped as a [Union] registered under union cases [CS].
     */
    fun wrap(): Union<CS> {
        return wrapUnion(obj as Any)
    }

    override fun toString(): String {
        return "${parent::class.simpleName}.${this::class.simpleName}(name=$name, ordinal=$ordinal)"
    }
}