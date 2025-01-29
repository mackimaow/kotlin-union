package io.github.mackimaow.kotlin.union

/**
 * Same as the [run] kotlin extension function, but specifically for Union types.
 * This function will only run the block if the union matches the [case].
 * @param case The case to check for
 * @param block The block to run if the case matches
 * @return An optional of the result of the block if the case matches, otherwise None
 * @see run
 */
inline fun <T, R, CS: UCases<CS>> Union<CS>.runWhen(
    case: UCase<CS, T>,
    block: T.() -> R
): Optional<R> {
    return case.unwrap(this).letSome(block)
}

/**
 * Same as the [also] kotlin extension function, but specifically for Union types.
 * This function will only run the block if the union matches the [case].
 * @param case The case to check for
 * @param block The block to run if the case matches
 * @return The union itself
 * @see also
 */
inline fun <T, CS: UCases<CS>> Union<CS>.alsoWhen(
    case: UCase<CS, T>,
    block: (T) -> Unit
): Union<CS> {
    case.unwrap(this).alsoSome(block)
    return this
}

/**
 * Same as the [apply] kotlin extension function, but specifically for Union types.
 * This function will only run the block if the union matches the [case].
 * @param case The case to check for
 * @param block The block to run if the case matches
 * @return The union itself
 * @see apply
 */
inline fun <T, CS: UCases<CS>> Union<CS>.applyWhen(
    case: UCase<CS, T>,
    block: T.() -> Unit
): Union<CS> {
    return alsoWhen(case, block)
}

/**
 * Same as the [let] kotlin extension function, but specifically for Union types.
 * This function will only run the block if the union matches the [case].
 * @param case The case to check for
 * @param block The block to run if the case matches
 * @return An optional of the result of the block if the case matches, otherwise None
 * @see let
 */
inline fun <T, R, CS: UCases<CS>> Union<CS>.letWhen(
    case: UCase<CS, T>,
    block: (T) -> R
): Optional<R> {
    return runWhen(case, block)
}

/**
 * Same as the [takeIf] kotlin extension function, but specifically for Union types.
 * This function will only run the block if the union matches the [case].
 * @param case The case to check for
 * @param block The block to run if the case matches
 * @return An optional of the value if the case matches and the block returns true, otherwise None
 * @see takeIf
 */
inline fun <T, CS: UCases<CS>> Union<CS>.takeIfWhen(
    case: UCase<CS, T>,
    block: (T) -> Boolean
): Optional<T> {
    return runWhen(case) {
        return if (block(this)) this.asSome() else Optional.None
    }
}

/**
 * Same as the [takeUnless] kotlin extension function, but specifically for Union types.
 * This function will only run the block if the union matches the [case].
 * @param case The case to check for
 * @param block The block to run if the case matches
 * @return An optional of the value if the case matches and the block returns false, otherwise None
 * @see takeUnless
 */
inline fun <T, CS: UCases<CS>> Union<CS>.takeUnlessWhen(
    case: UCase<CS, T>,
    block: (T) -> Boolean
): Optional<T> {
    return takeIfWhen(case) {
        !block(it)
    }
}

/**
 * Morphs the union into a new union based on the block (new union-specific type of control flow).
 * The receiver has all the same functions as the union itself and applies it to the current union (accessed by current).
 * The current union can be changed from calling the change functions: [MorphReceiver.changeWhen],
 * [MorphReceiver.changeByMorph], [MorphReceiver.changeByMorphingCase].
 * @param block The block to morph the union
 * @return The result of the block
 */
inline fun <R, CS: UCases<CS>> Union<CS>.morph(
    block: MorphReceiver<CS>.() -> R
): R {
    return MorphReceiver(this).block()
}

/**
 * Morphs the union into a new union based on the block (new union-specific type of control flow).
 * The receiver has all the same functions as the union itself and applies it to the current union (accessed by current).
 * The current union can be changed from calling the change functions: [MorphReceiver.changeWhen],
 * [MorphReceiver.changeByMorph], [MorphReceiver.changeByMorphingCase].
 * @param block The block to morph the union
 * @return The result of the block
 * @see morph
 */
inline fun <CS: UCases<CS>> Union<CS>.morphSelf(
    block: MorphReceiver<CS>.() -> Unit
): Union<CS> {
    val morphReceiver = MorphReceiver(this)
    morphReceiver.block()
    return morphReceiver.current
}

/**
 * A receiver class for morphing unions.
 * This class is used to change the current union in a block.
 * @param current The current union to be changed
 */
class MorphReceiver<CS: UCases<CS>> (var current: Union<CS>) {
    /**
     * Changes the current union to a new union if it matches the case, it will change according to [block].
     * @param case The case to check for
     * @param block The block to change the union
     */
    inline fun <T> changeWhen(
        case: UCase<CS, T>,
        block: T.() -> Union<CS>
    ) {
        current.applyWhen(case) {
            current = block()
        }
    }

    /**
     * Changes the current union to a new union if it matches the case, it will change according to [block].
     * When the case matches, the current union is morphed into a new union according to the morph [block].
     * @param case The case to check for
     * @param block The block to morph the union
     * @see io.github.mackimaow.kotlin.union.morph
     */
    inline fun <CS1: MatchCases<CS1>> changeByMorph(
        case: UnionCase<CS, CS1>,
        block: MorphReceiver<CS1>.() -> Union<CS>
    ) {
        current.applyWhen(case) {
            current = morph(block)
        }
    }

    /**
     * Changes the current union to a new union if it matches the case, it will change according to [block].
     * When the case matches, the current union is morphed into the same union type, but with
     * a different child union type.
     * @param case The case to check for
     * @param block The block to morph the union
     * @see io.github.mackimaow.kotlin.union.morphSelf
     */
    inline fun <CS1: MatchCases<CS1>> changeByMorphingCase(
        case: UnionCase<CS, CS1>,
        block: MorphReceiver<CS1>.() -> Unit
    ) {
        current.applyWhen(case) {
            current = morphSelf(block).wrapAs(case)
        }
    }

    /**
     * Makes a value based on the current union using [io.github.mackimaow.kotlin.union.morph]
     * @param block The block to change the union
     * @return The result of the block
     * @see io.github.mackimaow.kotlin.union.morph
     */
    inline fun <R> morph(
        block: MorphReceiver<CS>.() -> R
    ) = current.morph(block)

    /**
     * Makes a new union based on the current union using [io.github.mackimaow.kotlin.union.morphSelf]
     * @param block The block to change the union
     * @return The result of the block
     * @see io.github.mackimaow.kotlin.union.morphSelf
     */
    inline fun morphSelf(
        block: MorphReceiver<CS>.() -> Unit
    ) = current.morphSelf(block)

    /**
     * Performs [io.github.mackimaow.kotlin.union.runWhen] on the current union
     * @param case The case to check for
     * @param block The block to run if the case matches
     * @return An optional of the result of the block if the case matches, otherwise None
     * @see io.github.mackimaow.kotlin.union.runWhen
    */
    inline fun <T, R> runWhen(
        case: UCase<CS, T>,
        block: T.() -> R
    ) = current.runWhen(case, block)

    /**
     * Performs [io.github.mackimaow.kotlin.union.alsoSome] on the current union
     * @param case The case to check for
     * @param block The block to run if the case matches
     * @return The union itself
     * @see io.github.mackimaow.kotlin.union.alsoSome
     */
    inline fun <T> alsoWhen(
        case: UCase<CS, T>,
        block: (T) -> Unit
    ) = current.alsoWhen(case, block)

    /**
     * Performs [io.github.mackimaow.kotlin.union.applyWhen] on the current union
     * @param case The case to check for
     * @param block The block to run if the case matches
     * @return The union itself
     * @see io.github.mackimaow.kotlin.union.applyWhen
     */
    inline fun <T> applyWhen(
        case: UCase<CS, T>,
        block: T.() -> Unit
    ) = current.applyWhen(case, block)

    /**
     * Performs [io.github.mackimaow.kotlin.union.letWhen] on the current union
     * @param case The case to check for
     * @param block The block to run if the case matches
     * @return An optional of the result of the block if the case matches, otherwise None
     * @see io.github.mackimaow.kotlin.union.letWhen
     */
    inline fun <T, R> letWhen(
        case: UCase<CS, T>,
        block: (T) -> R
    ) = current.letWhen(case, block)

    /**
     * Performs [io.github.mackimaow.kotlin.union.takeIfWhen] on the current union
     * @param case The case to check for
     * @param block The block to run if the case matches
     * @return An optional of the value if the case matches and the block returns true, otherwise None
     * @see io.github.mackimaow.kotlin.union.takeIfWhen
     */
    inline fun <T> takeIfWhen(
        case: UCase<CS, T>,
        block: (T) -> Boolean
    ) = current.takeIfWhen(case, block)

    /**
     * Performs [io.github.mackimaow.kotlin.union.takeUnlessWhen] on the current union
     * @param case The case to check for
     * @param block The block to run if the case matches
     * @return An optional of the value if the case matches and the block returns false, otherwise None
     * @see io.github.mackimaow.kotlin.union.takeUnlessWhen
     */
    inline fun <T> takeUnlessWhen(
        case: UCase<CS, T>,
        block: (T) -> Boolean
    ) = current.takeUnlessWhen(case, block)
}
