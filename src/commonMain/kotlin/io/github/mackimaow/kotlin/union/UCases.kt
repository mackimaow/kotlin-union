package io.github.mackimaow.kotlin.union

import kotlin.reflect.KProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * This class is used to specify a set of cases that are to be used in a [Union].
 * The cases are specified by creating/registering objects that extend [MatchCases] or [DiscernCases], then
 * creating properties that represent cases of the union via property delegation from protected methods
 * [UCases.obj], [UCases.instance], [UCases.instanceWhen], [UCases.union], and [UCases.unionWhen].
 * @see [Union]
 * @see [MatchCases]
 * @see [DiscernCases]
 */
sealed class UCases<CS: UCases<CS>> {
    private val _cases: MutableList<UCase<CS, *>> = mutableListOf()
    private val _nameToCases: MutableMap<String, UCase<CS, *>> = mutableMapOf()
    protected val case: UCaseSupplier<CS> = UCaseSupplier()

    /**
     * @return the mapping of names to cases that are registered to this [UCases]
     */
    val nameToCase: Map<String, UCase<CS, *>> = _nameToCases

    /**
     * @return the list of cases in the order that they were registered in to this [UCases]
     */
    val cases: List<UCase<CS, *>> = _cases

    /**
     * Used to specify an object to be added to this [UCases].
     * @param T the type of the target object
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected fun <T> obj(
        obj: T
    ) = case.obj(obj)

    /**
     * Used to register a case that matches by a type check to this [UCases].
     * Must not use this function with reified argument [T] having a type with generic arguments.
     * @param T the type to check for the case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected inline fun <@TypeMustHaveNoGenericArgs reified T> instance() = case.instance<T>()

    /**
     * Used to specify a class that matches by a type check to this [UCases].
     * A conversion lambda [toType] is specified to combat type erasure when
     * checking types (see examples in [UCases]).
     * @param T the type to check for the case
     * @param toType the conversion lambda to be used to cast an instance to type [T]
     * if it is of type [T] otherwise [Optional.None]
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected fun <T> instance(
        toType: (Any?) -> Optional<T>
    ) = case.instance<T>(toType)

    /**
     * Used to specify a class of instances to be added to this [UCases].
     * Must not use this function with reified argument [T] having a type with generic arguments.
     * @param T the type to be added to the [UCases] and then to the [Union]
     * @param isCase the predicate lambda to be used to check if an instance is considered as this case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected inline fun <@TypeMustHaveNoGenericArgs reified T> instanceWhen(
        noinline isCase: (T) -> Boolean,
    ) = case.instanceWhen(isCase)

    /**
     * Used to specify a class of instances to be added to this [UCases]. A conversion lambda [toType]
     * is specified to combat type erasure when checking types (see examples in [UCases]).
     * @param T the type to be added to the [UCases] and then to the [Union]
     * @param toType the conversion lambda to be used to cast an instance to type [T]
     * if it is of type [T] otherwise [Optional.None]
     * @param isCase the predicate lambda to be used to check if an instance is considered as this case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected fun <T> instanceWhen(
        toType: (Any?) -> Optional<T>,
        isCase: (T) -> Boolean,
    ) = case.instanceWhen(toType, isCase)

    /**
     * Used to specify a [UCase] that is a union of instances.
     * @param CSChild the [MatchCases] type of the union to be added
     * @param cases the [MatchCases] of the union to be added
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected fun <CSChild: MatchCases<CSChild>> union(
        cases: CSChild
    ) = case.union(cases)

    /**
     * Used to specify a [UCase] that is a union of instances, further constrained by a predicate.
     * @param CSChild the [MatchCases] of the union to be added
     * @param cases the [MatchCases] of the union to be added
     * @param isCase the predicate lambda to be used to check if a union is considered as this case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    protected fun <CSChild: MatchCases<CSChild>> unionWhen(
        cases: CSChild,
        isCase: (Union<CSChild>) -> Boolean
    ) = case.unionWhen(cases, isCase)

    internal open fun assertNotRecursiveWithUnionCase(
        parentCase: UCases<*>,
        alreadyChecked: MutableSet<UCases<*>> = mutableSetOf()
    ) {
        if (this in alreadyChecked)
            return
        for (case in cases)
            if (case is UnionCase<*, *>)
                if (case.unionCases == parentCase)
                    throw IllegalArgumentException("$parentCase specifies a union recursively, which is not allowed.")
                else
                    case.unionCases.assertNotRecursiveWithUnionCase(parentCase, alreadyChecked)
        alreadyChecked.add(this)
    }

    internal open fun addCase(case: UCase<CS, *>) {
        _cases.add(case)
        _nameToCases[case.name] = case
    }
}

class UCaseSupplier<CS: UCases<CS>> internal constructor(){
    /**
     * Used to specify an object to be added to this [UCases].
     * @param T the type of the target object
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    fun <T> obj(
        obj: T
    ): UCaseProp<CS, T, ObjectCase<CS, T>> {
        val createCase = { name: String, ordinal: Int, parent: CS ->
            ObjectCase(obj, name, ordinal, parent)
        }
        return UCaseProp(createCase)
    }

    /**
     * Used to register a case that matches by a type check to this [UCases].
     * Must not use this function with reified argument [T] having a type with generic arguments.
     * @param T the type to check for the case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    inline fun <@TypeMustHaveNoGenericArgs reified T> instance(): UCaseProp<CS, T, InstanceCase<CS, T>> {
        return instance(
            T::class
        ) {
            if (it is T)
                Optional.Some(it)
            else
                Optional.None
        }
    }

    /**
     * Used to specify a class that matches by a type check to this [UCases].
     * A conversion lambda [toType] is specified to combat type erasure when
     * checking types (see examples in [UCases]).
     * @param T the type to check for the case
     * @param toType the conversion lambda to be used to cast an instance to type [T]
     * if it is of type [T] otherwise [Optional.None]
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    fun <T> instance(
        toType: (Any?) -> Optional<T>
    ): UCaseProp<CS, T, InstanceCase<CS, T>> {
        val createCase = { name: String, ordinal: Int, parent: CS ->
            InstanceCase(toType, name, ordinal, parent)
        }
        return UCaseProp(createCase)
    }

    /**
     * Don't use this function explicitly!
     */
    @Deprecated("This function is not meant to be used explicitly. Use inline function instance() instead.")
    fun <T> instance(
        type: KClass<*>,
        toType: (Any?) -> Optional<T>
    ): UCaseProp<CS, T, InstanceCase<CS, T>> {
        if (isUnionClass(type))
            throw IllegalArgumentException("Union type cannot be added via use of instance(). Use union() instead.")
        return instance(toType)
    }

    /**
     * Used to specify a class of instances to be added to this [UCases].
     * Must not use this function with reified argument [T] having a type with generic arguments.
     * @param T the type to be added to the [UCases] and then to the [Union]
     * @param isCase the predicate lambda to be used to check if an instance is considered as this case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    inline fun <@TypeMustHaveNoGenericArgs reified T> instanceWhen(
        noinline isCase: (T) -> Boolean,
    ): UCaseProp<CS, T, SpecificInstanceCase<CS, T>> {
        return instanceWhen(
            T::class,
            isCase
        )
    }

    /**
     * Used to specify a class of instances to be added to this [UCases]. A conversion lambda [toType]
     * is specified to combat type erasure when checking types (see examples in [UCases]).
     * @param T the type to be added to the [UCases] and then to the [Union]
     * @param toType the conversion lambda to be used to cast an instance to type [T]
     * if it is of type [T] otherwise [Optional.None]
     * @param isCase the predicate lambda to be used to check if an instance is considered as this case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    fun <T> instanceWhen(
        toType: (Any?) -> Optional<T>,
        isCase: (T) -> Boolean,
    ): UCaseProp<CS, T, SpecificInstanceCase<CS, T>> {
        val createCase = { name: String, ordinal: Int, parent: CS ->
            SpecificInstanceCase(isCase, toType, name, ordinal, parent)
        }
        return UCaseProp(createCase)
    }

    /**
     * Don't use this function explicitly!
     */
    @Deprecated("This function is not meant to be used explicitly. Use inline function instanceWhen() instead.")
    fun <T> instanceWhen(
        type: KClass<*>,
        isCase: (T) -> Boolean
    ): UCaseProp<CS, T, SpecificInstanceCase<CS, T>> {
        if (isUnionClass(type))
            throw IllegalArgumentException("Union type cannot be added via use of instanceWhen().")
        return instanceWhen(
            toType = {
                if (type.isInstance(it)) {
                    @Suppress("UNCHECKED_CAST")
                    Optional.Some(
                        it as T
                    )
                } else
                    Optional.None
            },
            isCase = isCase
        )
    }

    /**
     * Used to specify a [UCase] that is a union of instances.
     * @param CSChild the [MatchCases] type of the union to be added
     * @param cases the [MatchCases] of the union to be added
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    fun <CSChild: MatchCases<CSChild>> union(
        cases: CSChild
    ): UCaseProp<CS, Union<CSChild>, UnionCase<CS, CSChild>> {
        val createCase = { name: String, ordinal: Int, parent: CS ->
            cases.assertNotRecursiveWithUnionCase(parent)
            UnionCase(cases, name, ordinal, parent)
        }
        return UCaseProp(createCase)
    }

    /**
     * Used to specify a [UCase] that is a union of instances, further constrained by a predicate.
     * @param CSChild the [MatchCases] of the union to be added
     * @param cases the [MatchCases] of the union to be added
     * @param isCase the predicate lambda to be used to check if a union is considered as this case
     * @return [UCaseProp] the case to be registered as a property delegate
     */
    fun <CSChild: MatchCases<CSChild>> unionWhen(
        cases: CSChild,
        isCase: (Union<CSChild>) -> Boolean
    ): UCaseProp<CS, Union<CSChild>, SpecificUnionCase<CS, CSChild>> {
        val createCase = { name: String, ordinal: Int, parent: CS ->
            cases.assertNotRecursiveWithUnionCase(parent)
            SpecificUnionCase(cases, isCase, name, ordinal, parent)
        }
        return UCaseProp(createCase)
    }
}

/**
 * This represents a case to be registered as a property delegator to be used on [UCases] properties.
 * Delegation is used with the sole purpose of protecting the user from misuse
 * of protected member functions within [UCases].
 */
class UCaseProp<CS: UCases<CS>, T, C: UCase<CS, T>> internal constructor(
    internal val createCase: (String, Int, CS) -> C
){
    /**
     * Do not call this function explicitly (will cause erroneous behavior).
     * Use only with property delegation.
     * It is used to delegate the property to [UCases].
     */
    operator fun provideDelegate(thisRef: CS, prop: KProperty<*>): ReadOnlyProperty<CS, C> {
        val name = prop.name
        val ordinal = thisRef.cases.size
        val case = createCase(name, ordinal, thisRef)
        thisRef.addCase(case)
        return Delegate(case)
    }

    private inner class Delegate(
        val case: C
    ): ReadOnlyProperty<CS, C> {
        override operator fun getValue(
            thisRef: CS,
            property: KProperty<*>
        ): C {
            return case
        }
    }
}