package io.github.mackimaow.kotlin.union.test

import io.github.mackimaow.kotlin.union.MatchCases
import io.github.mackimaow.kotlin.union.Union
import kotlin.test.Test
import kotlin.test.assertFails

class UnionTestNative {
    object NumberCases: MatchCases<NumberCases>() {
        val INT by instance<Int>()
        val FLOAT by instance<Float>()
    }
    object NotARealUseButNeedToStillTestThis: MatchCases<NotARealUseButNeedToStillTestThis>() {
        fun bad() {
            instance<Union<NumberCases>>()
        }
        fun bad2() {
            instanceWhen<Union<NumberCases>> {
                true
            }
        }
    }

    @Test
    fun wrongfulInputOfUnion() {
        assertFails {
            NotARealUseButNeedToStillTestThis.bad()
        }
        assertFails {
            NotARealUseButNeedToStillTestThis.bad2()
        }
    }
}