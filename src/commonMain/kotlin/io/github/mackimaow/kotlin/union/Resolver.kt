package io.github.mackimaow.kotlin.union

/**
 * This is represents the receiver for:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 *  @param L [UnionOptions] that specifies the types for the [Union] of interest
 *  @param R The return type for ~~Union.map()~~ (**with** default)
 *  @property _state The [ResolverState] that manages the state of the map, trans, and alter
 *  expressions
 */
sealed interface Resolver<L: UnionOptions<L>, R> {
    val _state: ResolverState<L, R>
}


/**
 * Represents the 'accept' clause.
 * This should be used within the body of the resolve lambda inside:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 * The 'accept' clause head triggers running its body when the target
 * is of the type registered under [option].
 * Within these expressions, 'accept' allows the caller to transform the clause's target
 * into the return type then *break* the expression (similar to break in a loop).
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R the return type of the clause body
 * @param T that specifies the type of the [Union] to be triggered on
 * @see Union.map
 * @see Union.trans
 * @see Union.alter
 */
inline fun <L : UnionOptions<L>, R, T> Resolver<L, R>.accept(
    option: UnionOption<L, T>,
    body: ClauseBody<R>.(T) -> R
) {
    _state.runIfNotBroken {
        val unionValue = unwrapToAny(_state.union)
        if (option.discriminator(unionValue))
            @Suppress("UNCHECKED_CAST")
            _state.acceptAndBreak(_state.acceptReceiver.body(unionValue as T))
    }
}

/**
 * Represents the 'accept' clause.
 * This should be used within the body of the resolve lambda inside:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 * The 'accept' clause head triggers running its body when the target
 * is of the type registered under [option] and is equal to [specificOption].
 * Within these expressions, 'accept' allows the caller to transform the clause's target
 * into the return type then *break* the expression (similar to break in a loop).
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R the return type of the clause body
 * @param T that specifies the type of the [Union] to be triggered on
 * @see Union.map
 * @see Union.trans
 * @see Union.alter
 */
inline fun <L : UnionOptions<L>, R, T> Resolver<L, R>.accept(
    option: GeneralUnionOption<L, T>,
    specificOption: T,
    body: ClauseBody<R>.(T) -> R
) {
    _state.runIfNotBroken {
        val unionValue = unwrapToAny(_state.union)
        if (specificOption != null) {
            if (specificOption == unionValue)
                @Suppress("UNCHECKED_CAST")
                _state.acceptAndBreak(_state.acceptReceiver.body(unionValue as T))
        } else {
            if (option.discriminator(unionValue))
                @Suppress("UNCHECKED_CAST")
                _state.acceptAndBreak(_state.acceptReceiver.body(unionValue as T))
        }
    }
}

/**
 * Represents the 'change' clause.
 * This should be used within the body of the resolve lambda inside:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 * The 'change' clause head triggers running its body when the target
 * is of the type registered under [option].
 * Within these expressions, 'change' allows the caller to change the
 * clause's target into a new target and **doesn't**
 * *break* the expression.
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R the return type of the clause body
 * @param T that specifies the type of the [Union] to be triggered on
 * @see Union.map
 * @see Union.trans
 * @see Union.alter
 */
inline fun <L : UnionOptions<L>, R, T> Resolver<L, R>.change(
    option: UnionOption<L, T>,
    body: ClauseBody<Union<L>>.(T) -> Union<L>
) {
    _state.runIfNotBroken {
        val unionValue = unwrapToAny(_state.union)
        if (option.discriminator(unionValue))
            @Suppress("UNCHECKED_CAST")
            _state.union = _state.changeReceiver.body(unionValue as T)
    }
}

/**
 * Represents the 'change' clause.
 * This should be used within the body of the resolve lambda inside:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 * The 'change' clause head triggers running its body when the target
 * is of the type registered under [option] and is equal to [specificOption].
 * Within these expressions, 'change' allows the caller to change the
 * clause's target into a new target and **doesn't**
 * *break* the expression.
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R the return type of the clause body
 * @param T that specifies the type of the [Union] to be triggered on
 * @see Union.map
 * @see Union.trans
 * @see Union.alter
 */
inline fun <L : UnionOptions<L>, R, T> Resolver<L, R>.change(
    option: GeneralUnionOption<L, T>,
    specificOption: T,
    body: ClauseBody<Union<L>>.(T) -> Union<L>
) {
    _state.runIfNotBroken {
        val unionValue = unwrapToAny(_state.union)
        if (specificOption != null) {
            if (specificOption == unionValue)
                @Suppress("UNCHECKED_CAST")
                _state.union = _state.changeReceiver.body(unionValue as T)
        } else {
            if (option.discriminator(unionValue))
                @Suppress("UNCHECKED_CAST")
                _state.union = _state.changeReceiver.body(unionValue as T)
        }
    }
}

/**
 * Represents the 'execute' clause.
 * This should be used within the body of the resolve lambda inside:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 * The 'execute' clause head triggers running its body when the target
 * is of the type registered under [option].
 * Within these expressions, 'execute' allows the caller to *do something* **without** breaking
 * the expression or changing the target.
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R the return type of the clause body
 * @param T that specifies the type of the [Union] to be triggered on
 * @see Union.map
 * @see Union.trans
 * @see Union.alter
 */
inline fun <L : UnionOptions<L>, R, T> Resolver<L, R>.execute(
    option: UnionOption<L, T>,
    body: ClauseBody<Unit>.(T) -> Unit
) {
    _state.runIfNotBroken {
        val unionValue = unwrapToAny(_state.union)
        if (option.discriminator(unionValue))
            @Suppress("UNCHECKED_CAST")
            _state.executeReceiver.body(unionValue as T)
    }
}

/**
 * Represents the 'execute' clause.
 * This should be used within the body of the resolve lambda inside:
 * - ~~Union.map()~~
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 *
 * The 'execute' clause head triggers running its body when the target
 * is of the type registered under [option] and is equal to [specificOption].
 * Within these expressions, 'execute' allows the caller to *do something* **without** breaking
 * the expression or changing the target.
 *
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R the return type of the clause body
 * @param T that specifies the type of the [Union] to be triggered on
 * @see Union.map
 * @see Union.trans
 * @see Union.alter
 */
inline fun <L : UnionOptions<L>, R, T> Resolver<L, R>.execute(
    option: GeneralUnionOption<L, T>,
    specificOption: T,
    body: ClauseBody<Unit>.(T) -> Unit
) {
    _state.runIfNotBroken {
        val unionValue = unwrapToAny(_state.union)
        if (specificOption != null) {
            if (specificOption == unionValue)
                @Suppress("UNCHECKED_CAST")
                _state.executeReceiver.body(unionValue as T)
        } else {
            if (option.discriminator(unionValue))
                @Suppress("UNCHECKED_CAST")
                _state.executeReceiver.body(unionValue as T)
        }
    }
}