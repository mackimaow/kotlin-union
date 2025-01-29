package io.github.mackimaow.kotlin.union

import kotlin.jvm.JvmInline


/**
 * This is a sealed interface that represents an optional value.
 * It is similar to the Optional class in Java, but it is a sealed interface and supports nullable types.
 * @param T The type of the optional.
*/
sealed interface Optional<out T> {
    /**
     * Represents an optional with a value.
     * @param T The type of the optional.
     * @property value The value of the optional.
     */
    @JvmInline
    value class Some<T>(val value: T): Optional<T> {
        override fun getOrThrow(): T = value
        override fun toNullable(): T = value
        override val isSome: Boolean get() = true
    }

    /**
     * Represents a none optional.
     */
    data object None: Optional<Nothing> {
        override fun getOrThrow(): Nothing {
            throw NoSuchElementException("No value present")
        }
        override fun toNullable(): Nothing? = null
        override val isSome: Boolean = false
    }

    /**
     * @return the value of the optional if it is not None, otherwise it throws a NoSuchElementException.
     * @throws NoSuchElementException if the optional is None.
     */
    fun getOrThrow(): T

    /**
     * @return the value of the optional if it is not None, otherwise it returns null.
     */
    fun toNullable(): T?

    /**
     * @return true if the optional is not None, otherwise false.
     */
    val isSome: Boolean
}

/**
 * Returns the value of the optional if it is not None, otherwise it returns the default value.
 * @param default A lambda that returns a default value if the optional is None.
 * @return The value of the optional if it is not None, otherwise the default value.
 */
inline fun <T> Optional<T>.orElse(default: () -> T): T {
    return when(this) {
        is Optional.Some -> value
        is Optional.None -> default()
    }
}

/**
 * Converts a value to an optional.
 * @receiver The value to convert to an optional.
 * @return An optional with the value.
 */
fun <T> T.asSome(): Optional.Some<T> = Optional.Some(this)

/**
 * Converts a value to a none optional.
 * @receiver The value to convert to a none optional.
 * @return A none optional.
 */
fun <T> T.asNone() = Optional.None


/**
 * Converts a nullable value to an optional.
 * @receiver The nullable value to convert to an optional.
 * @return An optional with the value if it is not null, otherwise a none optional.
 */
fun <T> T?.asOptional(): Optional<T> {
    return if (this == null) Optional.None else Optional.Some(this)
}

/**
 * Maps the value of the optional to another value.
 * If the optional is none, it returns a None optional.
 * @param block The block to run the value of the optional.
 * @return An optional with the mapped value.
 */
inline fun <T, R> Optional<T>.runSome(block: T.() -> R): Optional<R> {
    return when(this) {
        is Optional.Some -> Optional.Some(block(value))
        is Optional.None -> Optional.None
    }
}

/**
 * Maps the value of the optional to another value.
 * If the optional is none, it returns a None optional.
 * @param block The block to run the value of the optional.
 * @return An optional with the mapped value.
 */
inline fun <T, R> Optional<T>.letSome(block: (T) -> R): Optional<R> {
    return runSome(block)
}


/**
 * Performs a block if the optional is not none.
 * @param block The block to perform if the optional is not none.
 * @return The optional itself.
 */
inline fun <T> Optional<T>.alsoSome(block: (T) -> Unit) {
    if (this is Optional.Some)
        block(value)
}

/**
 * Performs a block if the optional is not none.
 * @param block The block to perform if the optional is not none.
 * @return The optional itself.
 */
inline fun <T> Optional<T>.applySome(block: T.() -> Unit): Optional<T> {
    if (this is Optional.Some)
        value.block()
    return this
}


/**
 * Filters the value of the optional.
 * If the optional is none or the predicate returns false, it returns a None optional.
 * @param predicate The predicate which to filter the value of the optional.
 * @return A some optional if the value is not none and the predicate returns true, otherwise a None optional.
 */
inline fun <T> Optional<T>.takeIfSome(
    predicate: T.() -> Boolean
): Optional<T> {
    return when(this) {
        is Optional.Some -> if (predicate(value)) this else Optional.None
        is Optional.None -> Optional.None
    }
}


/**
 * Filters the value of the optional.
 * If the optional is none or the predicate returns true, it returns a None optional.
 * @param predicate The predicate which to filter the value of the optional.
 * @return A some optional if the value is not none and the predicate returns false, otherwise a None optional.
 */
inline fun <T> Optional<T>.takeUnlessSome(
    predicate: T.() -> Boolean
): Optional<T> {
    return when(this) {
        is Optional.Some -> if (!predicate(value)) this else Optional.None
        is Optional.None -> Optional.None
    }
}


/**
 * Flattens a double nested optional to an optional.
 * @receiver The double optional to flatten.
 * @return The flattened optional.
*/
fun <T> Optional<Optional<T>>.flatten(): Optional<T> {
    return when(this) {
        is Optional.Some -> value
        is Optional.None -> Optional.None
    }
}

