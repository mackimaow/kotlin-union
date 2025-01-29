package io.github.mackimaow.kotlin.union

/**
 * A UCases type that represents a different mode of specifying cases of a union.
 * The cases specific within this class only require differentiate against other cases within this class, and not
 * against all types even outside the union like [MatchCases] requires. Therefore, case checks here are less expensive
 * in terms of running time, but at the cost of losing [MatchCases.canWrap] and [MatchCases.wrap] functionality.
 * @param CS This class type.
 * @see MatchCases
 * @see UCases
 */
open class DiscernCases<CS: DiscernCases<CS>>: UCases<CS>()
