package io.github.mackimaow.kotlin.union.test

import io.github.mackimaow.kotlin.union.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UnionFunctionsTest {

    object NumberCases: MatchCases<NumberCases>() {
        val INT by instance<Int>()
        val LONG by instance<Long>()
    }

    object ColorCases: DiscernCases<ColorCases>() {
        val NUMBER by union(NumberCases)
        val RED by obj("red")
        val GREEN by obj("green")
        val BLUE by obj("blue")
    }

    val numberTestCases = listOf(
        1.wrapAs(NumberCases.INT),
        2L.wrapAs(NumberCases.LONG)
    )
    val allTestCases = listOf(
        *numberTestCases.map{
            it.wrapAs(ColorCases.NUMBER)
        }.toTypedArray(),
        ColorCases.RED.wrap(),
        ColorCases.GREEN.wrap(),
        ColorCases.BLUE.wrap()
    )

    @Test
    fun testUnionRunWhen() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.runWhen(NUMBER) {
                    runWhen(INT) {
                        this
                    } ?: runWhen(LONG) {
                        this.toInt()
                    } ?: -1
                } ?: color.runWhen(RED) {
                    3
                } ?: color.runWhen(GREEN) {
                    4
                } ?: color.runWhen(BLUE) {
                    5
                } ?: 6
            }
            val expectedResults = listOf(1, 2, 3, 4, 5)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testUnionAlsoWhen() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                var value = 6
                color.alsoWhen(NUMBER) { number ->
                    number.alsoWhen(INT) {
                        value = it
                    }.alsoWhen(LONG) {
                        value = it.toInt()
                    }
                }.alsoWhen(RED) {
                    value = 3
                }.alsoWhen(GREEN) {
                    value = 4
                }.alsoWhen(BLUE) {
                    value = 5
                }
                value
            }
            val expectedResults = listOf(1, 2, 3, 4, 5)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testUnionApplyWhen() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                var value = 6
                color.applyWhen(NUMBER) {
                    applyWhen(INT) {
                        value = this
                    }.applyWhen(LONG) {
                        value = this.toInt()
                    }
                }.applyWhen(RED) {
                    value = 3
                }.applyWhen(GREEN) {
                    value = 4
                }.applyWhen(BLUE) {
                    value = 5
                }
                value
            }
            val expectedResults = listOf(1, 2, 3, 4, 5)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testUnionLetWhen() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.letWhen(NUMBER) { number ->
                    number.letWhen(INT) {
                        it
                    } ?: number.letWhen(LONG) {
                        it.toInt()
                    } ?: -1
                } ?: color.letWhen(RED) {
                    3
                } ?: color.letWhen(GREEN) {
                    4
                } ?: color.letWhen(BLUE) {
                    5
                } ?: 6
            }
            val expectedResults = listOf(1, 2, 3, 4, 5)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testUnionTakeIfWhen() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.takeIfWhen(NUMBER) { number ->
                    number.takeIfWhen(INT) {
                        true
                    }?.run { true } ?: number.takeIfWhen(LONG) {
                        false
                    }?.run { true } ?: false
                }?.run { 1 } ?: color.takeIfWhen(RED) {
                    false
                }?.run { 2 } ?: color.takeIfWhen(GREEN) {
                    true
                }?.run { 3 } ?: color.takeIfWhen(BLUE) {
                    false
                }?.run { 4 } ?: -1
            }
            val expectedResults = listOf(1, -1, -1, 3, -1)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testUnionTakeUnlessWhen() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.takeUnlessWhen(NUMBER) { number ->
                    number.takeUnlessWhen(INT) {
                        true
                    }?.run { false } ?: number.takeUnlessWhen(LONG) {
                        false
                    }?.run { false } ?: true
                }?.run { 1 } ?: color.takeUnlessWhen(RED) {
                    false
                }?.run { 2 } ?: color.takeUnlessWhen(GREEN) {
                    true
                }?.run { 3 } ?: color.takeUnlessWhen(BLUE) {
                    false
                }?.run { 4 } ?: -1
            }
            val expectedResults = listOf(-1, 1, 2, -1, 4)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithRun() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    changeByMorph(NUMBER) {
                        runWhen(INT) {
                            RED.wrap()
                        }?.run {
                            return@changeByMorph this
                        }
                        runWhen(LONG) {
                            GREEN.wrap()
                        }?.run {
                            return@changeByMorph this
                        }
                        return@changeByMorph current.wrapAs(NUMBER)
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    } ?: false
                    changeWhen(BLUE) {
                        RED.wrap()
                    }
                    changeWhen(GREEN) {
                        BLUE.wrap()
                    }
                    if (!wasBlue)
                        changeWhen(RED) {
                            GREEN.wrap()
                        }
                    runWhen(NUMBER) {
                        runWhen(INT) {
                            1
                        } ?: runWhen(LONG) {
                            2
                        } ?: -1
                    } ?: runWhen(RED) {
                        3
                    } ?: runWhen(GREEN) {
                        4
                    } ?: runWhen(BLUE) {
                        5
                    } ?: 6
                }
            }
            val expectedResults = listOf(4, 5, 4, 5, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithApply() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    changeByMorph(NUMBER) {
                        var number = current.wrapAs(NUMBER)
                        applyWhen(INT) {
                            number = RED.wrap()
                        }
                        applyWhen(LONG) {
                            number = GREEN.wrap()
                        }
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    } ?: false
                    changeWhen(BLUE) {
                        RED.wrap()
                    }
                    changeWhen(GREEN) {
                        BLUE.wrap()
                    }
                    if (!wasBlue)
                        changeWhen(RED) {
                            GREEN.wrap()
                        }
                    var result = -1
                    applyWhen(NUMBER) {
                        applyWhen(INT) {
                            result = 1
                        }.applyWhen(LONG) {
                            result = 2
                        }
                    }
                    applyWhen(RED) {
                        result = 3
                    }
                    applyWhen(GREEN) {
                        result = 4
                    }
                    applyWhen(BLUE) {
                        result = 5
                    }
                    result
                }
            }
            val expectedResults = listOf(4, 5, 4, 5, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithAlso() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    changeByMorph(NUMBER) {
                        var number = current.wrapAs(NUMBER)
                        alsoWhen(INT) {
                            number = RED.wrap()
                        }
                        alsoWhen(LONG) {
                            number = GREEN.wrap()
                        }
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    } ?: false
                    changeWhen(BLUE) {
                        RED.wrap()
                    }
                    changeWhen(GREEN) {
                        BLUE.wrap()
                    }
                    if (!wasBlue)
                        changeWhen(RED) {
                            GREEN.wrap()
                        }
                    var result = -1
                    alsoWhen(NUMBER) {
                        result = -1
                    }
                    alsoWhen(RED) {
                        result = 3
                    }
                    alsoWhen(GREEN) {
                        result = 4
                    }
                    alsoWhen(BLUE) {
                        result = 5
                    }
                    result
                }
            }
            val expectedResults = listOf(4, 5, 4, 5, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithLet() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    changeByMorph(NUMBER) {
                        var number = current.wrapAs(NUMBER)
                        letWhen(INT) {
                            number = RED.wrap()
                        } ?: letWhen(LONG) {
                            number = GREEN.wrap()
                        }
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    } ?: false
                    changeWhen(BLUE) {
                        RED.wrap()
                    }
                    changeWhen(GREEN) {
                        BLUE.wrap()
                    }
                    if (!wasBlue)
                        changeWhen(RED) {
                            GREEN.wrap()
                        }
                    val result = runWhen(NUMBER) {
                        letWhen(INT) {
                            1
                        } ?: letWhen(LONG) {
                            2
                        } ?: -1
                    } ?: letWhen(RED) {
                        3
                    } ?: letWhen(GREEN) {
                        4
                    } ?: letWhen(BLUE) {
                        5
                    } ?: 6
                    result
                }
            }
            val expectedResults = listOf(4, 5, 4, 5, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithTakeIf() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    changeByMorph(NUMBER) {
                        var number = current.wrapAs(NUMBER)
                        takeIfWhen(INT) {
                            it == 1
                        }?.run {
                            number = RED.wrap()
                        } ?: takeIfWhen(LONG) {
                            it == 2L
                        }?.run {
                            number = GREEN.wrap()
                        }
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    } ?: false
                    changeWhen(BLUE) {
                        RED.wrap()
                    }
                    changeWhen(GREEN) {
                        BLUE.wrap()
                    }
                    if (!wasBlue)
                        changeWhen(RED) {
                            GREEN.wrap()
                        }
                    val result = runWhen(NUMBER) {
                        takeIfWhen(INT) {
                            true
                        }?.run {
                            1
                        } ?: takeIfWhen(LONG) {
                            true
                        }?.run {
                            2
                        } ?: -1
                    } ?: takeIfWhen(RED) {
                        true
                    }?.run {
                        3
                    } ?: takeIfWhen(GREEN) {
                        true
                    }?.run {
                        4
                    } ?: takeIfWhen(BLUE) {
                        true
                    }?.run {
                        5
                    } ?: 6
                    result
                }
            }
            val expectedResults = listOf(4, 5, 4, 5, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithTakeUnless() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    changeByMorph(NUMBER) {
                        var number = current.wrapAs(NUMBER)
                        takeUnlessWhen(INT) {
                            it != 1
                        }?.run {
                            number = RED.wrap()
                        } ?: takeUnlessWhen(LONG) {
                            it != 2L
                        }?.run {
                            number = GREEN.wrap()
                        }
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    } ?: false
                    changeWhen(BLUE) {
                        RED.wrap()
                    }
                    changeWhen(GREEN) {
                        BLUE.wrap()
                    }
                    if (!wasBlue)
                        changeWhen(RED) {
                            GREEN.wrap()
                        }
                    val result = runWhen(NUMBER) {
                        takeUnlessWhen(INT) {
                            false
                        }?.run {
                            1
                        } ?: takeUnlessWhen(LONG) {
                            false
                        }?.run {
                            2
                        } ?: -1
                    } ?: takeUnlessWhen(RED) {
                        false
                    }?.run {
                        3
                    } ?: takeUnlessWhen(GREEN) {
                        false
                    }?.run {
                        4
                    } ?: takeUnlessWhen(BLUE) {
                        false
                    }?.run {
                        5
                    } ?: 6
                    result
                }
            }
            val expectedResults = listOf(4, 5, 4, 5, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithMorph() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    morph {
                        runWhen(NUMBER) {
                            runWhen(INT) {
                                1
                            }?: runWhen(LONG) {
                                2
                            } ?: -1
                        } ?: -1
                    }
                }
            }
            val expectedResults = listOf(1, 2, -1, -1, -1)
            assertEquals(expectedResults, actualResults)
        } }
    }

    @Test
    fun testMorphWithMorphSelf() {
        with(ColorCases) { with(NumberCases) {
            val actualResults = allTestCases.map { color ->
                color.morph {
                    morphSelf {
                        changeWhen(NUMBER) {
                            RED.wrap()
                        }
                    }.runWhen(RED) {
                        1
                    } ?: runWhen(GREEN) {
                        2
                    } ?: runWhen(BLUE) {
                        3
                    } ?: 4
                }
            }
            val expectedResults = listOf(1, 1, 1, 2, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }
}