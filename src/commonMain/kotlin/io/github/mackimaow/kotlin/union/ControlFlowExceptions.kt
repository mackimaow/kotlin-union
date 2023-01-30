package io.github.mackimaow.kotlin.union


// for some reason, if I don't have these in an object, the compiler removes the single exception
// instance declaration
internal object ControlFlowExceptions {
    // Creating one exception is more efficient to be used everywhere in control flow
    internal val BREAK_RESOLVER_EXCEPTION = BreakResolverException()
    // Creating one exception is more efficient to be used everywhere in control flow
    internal val BREAK_EXCEPTION = BreakException()
    // Creating one exception is more efficient to be used everywhere in control flow
    internal val CONTINUE_EXCEPTION = ContinueException()
}