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
                    }.toNullable() ?: runWhen(LONG) {
                        this.toInt()
                    }.toNullable() ?: -1
                }.toNullable() ?: color.runWhen(RED) {
                    3
                }.toNullable() ?: color.runWhen(GREEN) {
                    4
                }.toNullable() ?: color.runWhen(BLUE) {
                    5
                }.toNullable() ?: 6
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
                    }.toNullable() ?: number.letWhen(LONG) {
                        it.toInt()
                    }.toNullable() ?: -1
                }.toNullable() ?: color.letWhen(RED) {
                    3
                }.toNullable() ?: color.letWhen(GREEN) {
                    4
                }.toNullable() ?: color.letWhen(BLUE) {
                    5
                }.toNullable() ?: 6
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
                    }.runSome { true }.toNullable() ?: number.takeIfWhen(LONG) {
                        false
                    }.runSome { true }.toNullable() ?: false
                }.runSome { 1 }.toNullable() ?: color.takeIfWhen(RED) {
                    false
                }.runSome { 2 }.toNullable() ?: color.takeIfWhen(GREEN) {
                    true
                }.runSome { 3 }.toNullable() ?: color.takeIfWhen(BLUE) {
                    false
                }.runSome { 4 }.toNullable() ?: -1
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
                    }.runSome { false }.toNullable() ?: number.takeUnlessWhen(LONG) {
                        false
                    }.runSome { false }.toNullable() ?: true
                }.runSome { 1 }.toNullable() ?: color.takeUnlessWhen(RED) {
                    false
                }.runSome { 2 }.toNullable() ?: color.takeUnlessWhen(GREEN) {
                    true
                }.runSome { 3 }.toNullable() ?: color.takeUnlessWhen(BLUE) {
                    false
                }.runSome { 4 }.toNullable() ?: -1
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
                        }.runSome {
                            return@changeByMorph this
                        }
                        runWhen(LONG) {
                            GREEN.wrap()
                        }.runSome {
                            return@changeByMorph this
                        }
                        return@changeByMorph current.wrapAs(NUMBER)
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    }.toNullable() ?: false
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
                        }.toNullable() ?: runWhen(LONG) {
                            2
                        }.toNullable() ?: -1
                    }.toNullable() ?: runWhen(RED) {
                        3
                    }.toNullable() ?: runWhen(GREEN) {
                        4
                    }.toNullable() ?: runWhen(BLUE) {
                        5
                    }.toNullable() ?: 6
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
                    }.toNullable() ?: false
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
                    }.toNullable() ?: false
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
                        }.toNullable() ?: letWhen(LONG) {
                            number = GREEN.wrap()
                        }
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    }.toNullable() ?: false
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
                        }.toNullable() ?: letWhen(LONG) {
                            2
                        }.toNullable() ?: -1
                    }.toNullable() ?: letWhen(RED) {
                        3
                    }.toNullable() ?: letWhen(GREEN) {
                        4
                    }.toNullable() ?: letWhen(BLUE) {
                        5
                    }.toNullable() ?: 6
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
                        }.runSome {
                            number = RED.wrap()
                        }.toNullable() ?: takeIfWhen(LONG) {
                            it == 2L
                        }.runSome {
                            number = GREEN.wrap()
                        }.toNullable()
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    }.toNullable() ?: false
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
                        }.runSome {
                            1
                        }.toNullable() ?: takeIfWhen(LONG) {
                            true
                        }.runSome {
                            2
                        }.toNullable() ?: -1
                    }.toNullable() ?: takeIfWhen(RED) {
                        true
                    }.runSome {
                        3
                    }.toNullable() ?: takeIfWhen(GREEN) {
                        true
                    }.runSome {
                        4
                    }.toNullable() ?: takeIfWhen(BLUE) {
                        true
                    }.runSome {
                        5
                    }.toNullable() ?: 6
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
                        }.runSome {
                            number = RED.wrap()
                        }.toNullable() ?: takeUnlessWhen(LONG) {
                            it != 2L
                        }.runSome {
                            number = GREEN.wrap()
                        }.toNullable()
                        number
                    }
                    val wasBlue = runWhen(BLUE) {
                        true
                    }.toNullable() ?: false
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
                        }.runSome {
                            1
                        }.toNullable() ?: takeUnlessWhen(LONG) {
                            false
                        }.runSome {
                            2
                        }.toNullable() ?: -1
                    }.toNullable() ?: takeUnlessWhen(RED) {
                        false
                    }.runSome {
                        3
                    }.toNullable() ?: takeUnlessWhen(GREEN) {
                        false
                    }.runSome {
                        4
                    }.toNullable() ?: takeUnlessWhen(BLUE) {
                        false
                    }.runSome {
                        5
                    }.toNullable() ?: 6
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
                            }.toNullable() ?: runWhen(LONG) {
                                2
                            }.toNullable() ?: -1
                        }.toNullable() ?: -1
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
                    }.toNullable() ?: runWhen(GREEN) {
                        2
                    }.toNullable() ?: runWhen(BLUE) {
                        3
                    }.toNullable() ?: 4
                }
            }
            val expectedResults = listOf(1, 1, 1, 2, 3)
            assertEquals(expectedResults, actualResults)
        } }
    }
}