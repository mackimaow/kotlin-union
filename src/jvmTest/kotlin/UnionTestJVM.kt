import io.github.mackimaow.kotlin.test.GeneralUseCaseTest.NumberCases
import io.github.mackimaow.kotlin.union.MatchCases
import io.github.mackimaow.kotlin.union.Union
import kotlin.test.Test
import kotlin.test.assertFails

class UnionTestJVM {
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