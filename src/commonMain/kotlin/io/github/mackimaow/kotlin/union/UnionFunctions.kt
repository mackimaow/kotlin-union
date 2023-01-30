package io.github.mackimaow.kotlin.union


/**
 *
 * Maps a [Union] instance into another type with a [default] option. This is similar to a when expression
 * where the cases are specified in lambda [resolve]. One can use any [UnionOption]
 * within [UnionOptions] specified by [L] to declare clause cases and their bodies.
 * Given a [UnionOption] there are several types of clause transformations to choose from:
 * -  ~~Resolver.accept(option)~~: allows the caller to transform the clause's target
 *  into the return type then *break* the ~~Union.map()~~ expression (similar to break in a loop).
 * - ~~Resolver.change(option)~~: allows the caller to change the clause's target into a new target and **doesn't**
 *  *break* the ~~Union.map()~~ expression.
 * - ~~Resolver.execute(option)~~: allows the caller to *do something* **without** breaking
 *  the ~~Union.map()~~ expression or changing the target.
 *
 * The caller can decide to use Break and Continue
 * within any clause body (similar to for loops).
 *
 * Example:
 * ```
 * // Declaring Union of:
 * //  Int | "green" | "blue" | "red"
 * object Color: UnionOptions<Color>({Color}) {
 *      val INT = option<Int>()
 *      val GREEN = literal("green")
 *      val BLUE = literal("blue")
 *      val RED = literal("red")
 * }
 *
 * fun isFavoriteColor(color: Union<Color>): Boolean {
 *      return color.map(false) {
 *          change(Color.INT) { colorAsHex ->
 *              if(colorAsHex < 256)
 *                  Color.BLUE.wrap() // basically a blue color
 *              else
 *                  Break  // every thing else I don't care about
 *          }
 *          execute(Color.GREEN) { greenString ->
 *              println("I don't like green!")
 *          }
 *          accept(Color.BLUE) { blueString ->
 *              println("I love blue!")
 *              true
 *          }
 *      }
 * }
 * ```
 *
 * @param L The [UnionOptions] indicating the composition of options of the union.
 * @param R The resulting type of the mapping.
 * @receiver Union
 * @return [default] if no ~~Resolver.accept()~~ clauses called within [resolve] are triggered and completed,
 * otherwise it will return result of the last ~~Resolver.accept()~~ clause that completed
 * @see UnionOptions
 * @see UnionOption
 * @see Resolver
 * @see ClauseBody
 */
inline fun <L: UnionOptions<L>, R> Union<L>.map(
    default: R,
    resolve: Resolver<L, R>.() -> Unit
): R {
    val state = ResolverState<L, R>(this)
    val resolver = RegularResolver(state)
    state.runUntilResolverIsBroken {
        resolver.resolve()
    }
    if (state.isValueFound()) {
        @Suppress("UNCHECKED_CAST")
        return state.getNullableValue() as R
    } else {
        return default
    }
}


/**
 * Maps a [Union] instance into another type. This is similar to a when expression
 * where the cases are specified in lambda [resolve]. One can use any [UnionOption]
 * within [UnionOptions] specified by [L] to declare clause cases and their bodies.
 * Given a [UnionOption] there are several types of clause transformations to choose from:
 *  - ~~Resolver.accept(option)~~: allows the caller to transform the clause's target
 *  into the return type then *break* the ~~Union.map()~~ expression (similar to break in a loop).
 *  - ~~Resolver.change(option)~~: allows the caller to change the clause's target into a new target and **doesn't**
 *  *break* the ~~Union.map()~~ expression.
 *  - ~~Resolver.execute(Resolver)~~: allows the caller to *do something* **without** breaking
 *  the ~~Union.map()~~ expression or changing the target.
 *
 *  The caller **must** specify an *otherwise* block in this expression or a runtime exception is thrown
 *  The caller can decide to use Break and Continue within any clause body (similar to for loops).
 *
 * Example:
 * ```
 * // Declaring Union of:
 * //  Int | "green" | "blue" | "red"
 * object Color: UnionOptions<Color>({Color}) {
 *      val INT = option<Int>()
 *      val GREEN = literal("green")
 *      val BLUE = literal("blue")
 *      val RED = literal("red")
 * }
 *
 * fun isFavoriteColor(color: Union<Color>): Boolean {
 *      return color.map {
 *          change(Color.INT) { colorAsHex ->
 *              if(colorAsHex < 256)
 *                  Color.BLUE.wrap() // basically a blue color
 *              else
 *                  Break  // every thing else I don't care about
 *          }
 *          execute(Color.GREEN) { greenString ->
 *              println("I don't like green!")
 *          }
 *          accept(Color.BLUE) { blueString ->
 *              println("I love blue!")
 *              true
 *          }
 *          otherwise { colorUnion ->
 *              false
 *          }
 *      }
 * }
 * ```
 *
 * @throws MissingOtherwiseClause if the otherwise block is not called when needed within [resolve].
 * @see UnionOptions
 * @see UnionOption
 * @see Resolver
 * @see ClauseBody
 * @param L The [UnionOptions] indicating the composition of options of the [Union].
 * @param R The resulting type of the mapping.
 * @receiver [Union]
 * @return the result from the otherwise block if no ~~Resolver.accept()~~ clauses called within [resolve]
 * are triggered and completed, otherwise it will return result of the last ~~Resolver.accept()~~ clause
 * that completed.
 */
inline fun <L: UnionOptions<L>, R> Union<L>.map(
    resolve: OtherwiseResolver<L, R>.() -> Unit
): R {
    val state = ResolverState<L, R>(this)
    val resolver = OtherwiseResolver<L, R>(state)
    state.runUntilResolverIsBroken {
        resolver.resolve()
    }
    return state.getValue()
}


/**
 * Transforms a [Union] instance into another instance of the [Union].
 * This is similar to a when expression where the cases are specified in lambda [resolve].
 * One can use any [UnionOption]
 * within [UnionOptions] specified by [L] to declare clause cases and their bodies.
 * Given a [UnionOption] there are several types of clause transformations to choose from:
 * -  ~~Resolver.accept(option)~~: allows the caller to transform the clause's target
 *  into the return type then *break* the ~~Union.trans()~~ expression (similar to break in a loop).
 * -  ~~Resolver.change(option)~~: allows the caller to change the clause's target into a new target and **doesn't**
 *  *break* the ~~Union.trans()~~ expression.
 *  - ~~Resolver.execute(option)~~: allows the caller to *do something* **without** breaking
 *  the ~~Union.trans()~~ expression or changing the target.
 *
 *  The caller can decide to use Break and Continue
 *  within any clause body (similar to for loops).
 *
 * Example:
 * ```
 * // Declaring Union of:
 * //  Int | "green" | "blue" | "red"
 * object Color: UnionOptions<Color>({Color}) {
 *      val INT = option<Int>()
 *      val GREEN = literal("green")
 *      val BLUE = literal("blue")
 *      val RED = literal("red")
 * }
 *
 * fun colorAsHexWithAlpha(color: Union<Color>): Union<Color> {
 *      return color.trans {
 *          execute(Color.INT) {
 *              println("I don't need to 'change' clause, I'm already as a hex")
 *          }
 *          change(Color.RED) { redString ->
 *              Color.Int.wrap(0xFF0000)
 *          }
 *          change(Color.GREEN) { greenString ->
 *              Color.Int.wrap(0x00FF00)
 *          }
 *          change(Color.BLUE) { blueString ->
 *              Color.Int.wrap(0x0000FF)
 *          }
 *          accept(Color.INT) { colorAsHex ->
 *              Color.Int.wrap(colorAsHex | 0xFF000000)
 *          }
 *      }
 * }
 * ```
 *
 * @see UnionOptions
 * @see UnionOption
 * @see Resolver
 * @see ClauseBody
 * @param L The [UnionOptions] indicating the composition of options of the [Union].
 * @receiver [Union]
 * @return the result from the otherwise block if no ~~Resolver.accept()~~ clauses called within [resolve]
 * are triggered and completed or original union if no otherwise block exists.
 * Otherwise, it will return result of the last ~~Resolver.accept()~~ clause that completed.
 */
inline fun <L: UnionOptions<L>> Union<L>.trans(
    resolve: OtherwiseResolver<L, Union<L>>.() -> Unit
): Union<L> {
    val state = ResolverState<L, Union<L>>(this)
    val resolver = OtherwiseResolver(state)
    state.runUntilResolverIsBroken {
        resolver.resolve()
    }
    return try {
        state.getValue()
    } catch(e: MissingOtherwiseClause) {
        this
    }
}

/**
 * Alters a [Union] instance.
 * This is similar to a when expression where the cases are specified in lambda [resolve].
 * One can use any [UnionOption]
 * within [UnionOptions] specified by [L] to declare clause cases and their bodies.
 * Given a [UnionOption] there are several types of clause transformations to choose from:
 * - ~~Resolver.accept(option)~~: allows the caller to *do something* to the
 * clause's target then *break* the ~~Union.alter()~~ expression (similar to break in a loop).
 * - ~~Resolver.change(option)~~: allows the caller to change the clause's target into a new target and **doesn't**
 * *break* the ~~Union.alter()~~ expression.
 * - ~~Resolver.execute(option)~~: allows the caller to *do something* **without** breaking
 * the ~~Union.alter()~~ expression or changing the target.
 *
 * The caller can decide to use Break and Continue
 * within any clause body (similar to for loops).
 *
 * Example:
 * ```
 * // Declaring Union of:
 * //  Int | "green" | "blue" | "red"
 * object Color: UnionOptions<Color>({Color}) {
 *      val INT = option<Int>()
 *      val GREEN = literal("green")
 *      val BLUE = literal("blue")
 *      val RED = literal("red")
 * }
 *
 * fun isThisYourFavoriteColor(color: Union<Color>) {
 *      color.alter() {
 *          change(Color.INT) { colorAsHex ->
 *              if(colorAsHex < 256)
 *                  Color.BLUE.wrap() // basically a blue color
 *              else
 *                  Break  // every thing else I don't care about
 *          }
 *          execute(Color.GREEN) { greenString ->
 *              println("I don't like green!")
 *          }
 *          accept(Color.BLUE) { blueString ->
 *              println("I love blue!")
 *          }
 *          otherwise { color ->
 *              println("I have no preference for color '$color'")
 *          }
 *      }
 * }
 * ```
 *
 * @see UnionOptions
 * @see UnionOption
 * @see Resolver
 * @see ClauseBody
 * @param L The [UnionOptions] indicating the composition of options of the [Union].
 * @receiver [Union]
 * @return the original receiver [Union] that called ~~Union.alter()~~
 */
inline fun <L: UnionOptions<L>> Union<L>.alter(
    resolve: OtherwiseResolver<L, Unit>.() -> Unit
): Union<L> {
    val state = ResolverState<L, Unit>(this)
    val resolver = OtherwiseResolver(state)
    state.runUntilResolverIsBroken {
        resolver.resolve()
    }
    return this
}

