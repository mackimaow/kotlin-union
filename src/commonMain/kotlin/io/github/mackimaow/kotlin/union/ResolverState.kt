package io.github.mackimaow.kotlin.union


/**
 * This is thrown during a call for ~~Union.map()~~ (without default) when an otherwise clause
 * block is not specified.
 */
class MissingOtherwiseClause internal constructor(): RuntimeException("Missing Otherwise Clause in map")

class BreakResolverException internal constructor(): RuntimeException()
// Creating one exception is more efficient to be used in control flow
internal val BREAK_RESOLVER_EXCEPTION = BreakResolverException()


/**
 * This manages the state after a call to:
 *  - ~~Union.map()~~ (without default)
 *  - ~~Union.trans()~~
 *  - ~~Union.alter()~~
 *
 * Most of the functionality/fields public presented here is not for the caller
 * to use these features directly, but to use them indirectly through inline functions.
 * For this reason, no further documentation for particular components within [ResolverState]
 * will be described.
 * @param L [UnionOptions] specifying the union types for the [Union]
 * @param R The return type for the resolver
 */
@Deprecated(
    message = "Use functions Union.map, Union.trans, Union.alter"
)
class ResolverState<L: UnionOptions<L>, R>(
    var union: Union<L>
) {
    private var value: R? = null
    private var foundValue = false
    private var broken = false
    private var otherwiseClauseFound: Boolean = false

    val acceptReceiver = ClauseBody<R>(this)
    val changeReceiver = ClauseBody<Union<L>>(this)
    val executeReceiver = ClauseBody<Unit>(this)

    private val Break: Any
        get() = throw BREAK_RESOLVER_EXCEPTION


    fun setOtherwiseFound() {
        this.otherwiseClauseFound = true
    }

    fun isValueFound(): Boolean {
        return foundValue
    }

    fun isBroken(): Boolean {
        return broken
    }

    fun setBroken() {
        broken = true
    }

    fun getNullableValue(): R? {
        return value
    }

    fun getValue(): R {
        if (foundValue)
            @Suppress("UNCHECKED_CAST")
            return value as R
        else
            throw MissingOtherwiseClause()
    }

    fun acceptAndBreak(value: R) {
        foundValue = true
        this.value = value
        setBroken()
        Break
    }

    inline fun runUntilResolverIsBroken(body: () -> Unit) {
        try {
            body()
        } catch (_: BreakResolverException) {}
    }

    inline fun runIfNotBroken(body: () -> Unit) {
        if (!isBroken()) {
            try {
                body()
            } catch (_: BreakException) {
                setBroken()
            } catch (_: ContinueException) {}
        }
    }
}