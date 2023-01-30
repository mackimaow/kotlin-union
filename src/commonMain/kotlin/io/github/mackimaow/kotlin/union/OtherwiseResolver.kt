package io.github.mackimaow.kotlin.union

import kotlin.jvm.JvmInline


/**
 * This is represents the receiver for:
 * - ~~Union.map()~~ (without default)
 * - ~~Union.trans()~~
 * - ~~Union.alter()~~
 * @param L [UnionOptions] that specifies what types are within the union
 * @param R The return type for ~~Union.map()~~, ~~Union.trans()~~, or ~~Union.alter()~~
 */
@JvmInline
value class OtherwiseResolver<L: UnionOptions<L>, R> (
    override val _state: ResolverState<L, R>
): Resolver<L, R> {
    /**
     * This defines a clause body '[otherwiseClause]' that is called when not a single ~~Resolver.accept()~~ clause body
     * completed without invoking Break or Continue within a ~~Union.map()~~, ~~Union.trans()~~,
     * or ~~Union.alter()~~ expression. Note, all clauses that follow otherwise are effectively dead code.
     */
    inline fun otherwise(otherwiseClause: (Union<L>) -> R) {
        _state.setOtherwiseFound()
        val union = _state.union
        if (!_state.isValueFound())
            _state.acceptAndBreak(otherwiseClause(union))
    }
}