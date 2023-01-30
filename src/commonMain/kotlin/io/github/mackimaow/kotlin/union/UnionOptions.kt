package io.github.mackimaow.kotlin.union

/**
 * This exception is thrown when the caller tries to add options to an already existing [UnionOptions]
 * object that they do not have permission to extend.
 */
class WrongUnionOptionsType internal constructor(
    receiver: UnionOptions<*>, other: UnionOptions<*>
): RuntimeException(
    "Union Options instance '$receiver' tried to add options to an already existing" +
    " Union Options instance '$other'"
)

/**
 * Although [Union] represents a Union type, [UnionOptions] *defines* the [Union]
 * by listing types and literals that exist within the union.
 * This is done by creating a singleton object that inherits [UnionOptions] and
 * creating val parameters which are *set* to [UnionOption] type parameterized with
 * the types of the union (as one would do when creating an enumeration).
 * One cannot instantiate [UnionOption] by itself, however one can create them through
 * protected methods ~~UnionOption.option()~~ or ~~UnionOption.literal()~~
 * Here is a simple example of a [UnionOption] object:
 *
 * ```
 * // Union of:
 * //    Int | "red" | "blue" | "green"
 * object Color: UnionOptions<Color>({Color}) {
 *      val INT = option<Int>()
 *      val RED = literal("red")
 *      val BLUE = literal("blue")
 *      val GREEN = literal("green")
 * }
 * // Union usage:
 * fun createColor(hexValue: Int): Union<Color> {
 *      return Color.INT.wrap(hexValue) // *or* Color.wrap(hexValue)!!
 * }
 *
 * fun createRed(): Union<Color> {
 *      return Color.RED.wrap() // *or* Color.wrap("red")!!
 * }
 *
 * fun createBlue(): Union<Color> {
 *      return Color.BLUE.wrap() // *or* Color.wrap("blue")!!
 * }
 *
 * fun createGreen(): Union<Color> {
 *      return Color.GREEN.wrap() // *or* Color.wrap("green")!!
 * }
 * ```
 *
 * To create a [UnionOptions] object, it can be seen that one must provide a lambda expression
 * [getInstance] that retrieves the [UnionOptions] object created. This is to
 * ensure that other callers cannot add unjustly add more types to the union that
 * were not originally intended (as sort of makeshift virtual visibility modifier one can say).
 *
 * There are some complications with type erasure when supplying types to the union that have
 * generic parameters. [UnionOptions] uses runtime type checks to discriminate between types in
 * the union. Type Erasure is where this falls short. Therefore, when one adds types with
 * generic parameters, they must supply an additional argument to [UnionOptions.option()] call
 * . This additional argument is a discriminator, which is a predicate that returns true
 * given that the input is an instance of the type specified by the option, false otherwise.
 *
 * Some cases, it's impossible to tell what specific type parameter given an instance. For example,
 * a List<T> instance that is an empty list. For these cases, it is highly recommended to add an
 * option to the [UnionOptions] of the ambiguous case.
 * For example:
 * ```
 * // Union of:
 * //    List<Int> | List<String>
 * object Colors: UnionOptions<Colors>({Colors}) {
 *      val INTS = option<List<Int>> {  // I specified a discriminator predicate for List<Int>
 *          it is List<*> && it.isNotEmpty() && it[0] is Int
 *      }
 *      val STRINGS = option<List<String>> {  // I specified a discriminator predicate for List<String>
 *          it is List<*> && it.isNotEmpty() && it[0] is String
 *      }
 *      val AMBIGUOUS_LIST = option<List<*>> {  // I added an option for the ambiguous list
 *          it is List<*> && it.isEmpty()
 *      }
 * }
 *
 * ```
 *
 * @param L The type of the object created (itself)
 */
open class UnionOptions<L: UnionOptions<L>>(private val getInstance: () -> L) {
    private val options: MutableList<UnionOption<L, *>> = mutableListOf()

    private fun <T> addToOptions(option: UnionOption<L, T>){
        if (this != getInstance())
            throw WrongUnionOptionsType(this, getInstance())
        options.add(option)
    }

    /**
     * Used to specify a class of instances to be added to this [UnionOptions] by
     * creating/registering an object of type [GeneralUnionOption].
     * @param T the type to be added to the [UnionOptions] and then to the [Union]
     * @return [GeneralUnionOption] the registered option for the class of instances
     */
    protected inline fun <reified T> option(): GeneralUnionOption<L, T> {
        val createdOption = option<T> {
            it is T
        }
        return createdOption
    }

    /**
     * Used to specify a class of instances to be added to this [UnionOptions] by
     * creating/registering an object of type [GeneralUnionOption]. A [discriminator]
     * is specified to combat type erasure when checking types (see examples in [UnionOptions]).
     * @param T the type to be added to the [UnionOptions] and then to the [Union]
     * @see UnionOptions
     * @return [GeneralUnionOption] the registered option for the class of instances
     */
    protected fun <T> option(
        discriminator: ((Any) -> Boolean)
    ): GeneralUnionOption<L, T> {
        val createdOption = GeneralUnionOption<L, T>(discriminator)
        addToOptions(createdOption)
        return createdOption
    }

    /**
     * Used to specify a literal to be added to this [UnionOptions] by
     * creating/registering an object of type [UnionLiteral].
     * @param T the type of the literal to be added to the [UnionOptions] and then to the [Union]
     * @see UnionOptions
     * @return [UnionLiteral] the registered option for the literal
     */
    protected fun <T> literal(
        literal: T
    ): UnionLiteral<L, T> {
        val createdOption = UnionLiteral<L, T>(literal)
        addToOptions(createdOption)
        return createdOption
    }

    /**
     * @return whether the instance [obj] can be wrapped as a type for this [Union].
     * This is indicated by what classes and literals are registered by this [UnionOptions]
     */
    fun canBeWrapped(obj: Any): Boolean {
        for (option in options)
            if(option.discriminator(obj))
                return true
        return false
    }

    /**
     * @return the instance [obj] wrapped as a type for this [Union], or
     * ~~null~~ if it can't.
     * This is indicated by what classes and literals are registered by this [UnionOptions]
     */
    fun wrap(obj: Any): Union<L>? {
        if (canBeWrapped(obj))
            return wrapUnion(obj)
        return null
    }
}