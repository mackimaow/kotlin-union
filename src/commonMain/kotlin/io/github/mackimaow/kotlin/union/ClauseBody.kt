package io.github.mackimaow.kotlin.union

import kotlin.jvm.JvmInline


class BreakException internal constructor(): RuntimeException()
// Creating one exception is more efficient to be used in control flow
internal val BREAK_EXCEPTION = BreakException()
class ContinueException internal constructor(): RuntimeException()
// Creating one exception is more efficient to be used in control flow
internal val CONTINUE_EXCEPTION = ContinueException()

/**
 * The *receiver* of a clause's body when using
 * ~~Resolver.accept()~~ ~~Resolver.change()~~ or ~~Resolver.execute()~~ clauses
 * inside a ~~Union.map()~~ ~~Union.trans()~~ or ~~Union.alter()~~ expression
 *
 * @param R The return type of clause's functional body.
 * @property Break a property, when accessed, acts as a **break** (similar to it's meaning in loops) for the ~~Union.map()~~
 * ~~Union.trans()~~ or ~~Union.alter()~~ expression being called. If an otherwise block exists, accessing Break will
 * immediately go into the otherwise block (if it exists) and skip the rest of code within the current clause body.
 * @property Continue a property, when accessed, acts as a **continue** (similar to it's meaning loops) for the ~~Union.map()~~
 * ~~Union.trans()~~ or ~~Union.alter()~~ expression being called. Accessing Continue will skip the rest of code within
 * the current clause body and will continue checking & performing subsequent clauses
 */
@JvmInline
value class ClauseBody<R> internal constructor(
    val value: ResolverState<*, *>
) {
    val Break: R
        get() = throw BREAK_EXCEPTION
    val Continue: R
        get() = throw CONTINUE_EXCEPTION
}