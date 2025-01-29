package io.github.mackimaow.kotlin.union.test

import kotlin.test.Test
import io.github.mackimaow.kotlin.union.*

class DocExamplesTest {

    // FROM: src/commonMain/kotlin/io/github/mackimaow/kotlin/union/UCases.kt

    // Union of:
    //    Int | "red" | "blue" | "green"
    object ColorCases: MatchCases<ColorCases>() {
        val INT by instance<Int>()
        val RED by obj("red")
        val BLUE by obj("blue")
        val GREEN by obj("green")
    }
    // Union usage:
    fun createColor(hexValue: Int): Union<ColorCases> {
        hexValue.wrapAs(ColorCases).getOrThrow()
        return hexValue.wrapAs(ColorCases.INT) // *or* hexValue.wrapAs(ColorCases)!!
    }
    fun createRed(): Union<ColorCases> {
        "red".wrapAs(ColorCases).getOrThrow()
        return ColorCases.RED.wrap() // *or* "red".wrapAs(ColorCases)!!
    }
    fun createBlue(): Union<ColorCases> {
        "blue".wrapAs(ColorCases).getOrThrow()
        return ColorCases.BLUE.wrap() // *or* "blue".wrapAs(ColorCases)!!
    }
    fun createGreen(): Union<ColorCases> {
        "green".wrapAs(ColorCases).getOrThrow()
        return ColorCases.GREEN.wrap() // *or* "green".wrapAs(ColorCases)!!
    }

    // Union of:
    //    List<Int> | List<String>
    object ColorListCases: MatchCases<ColorListCases>() {
        val INTS by instance<List<Int>> {  // I specified a typeCast for List<Int>
            (it as? List<*>).asOptional().takeIfSome {
                isNotEmpty() && this[0] is Int
            }.letSome { list ->
                @Suppress("UNCHECKED_CAST")
                list as List<Int>
            }
        }
        val STRINGS by instance<List<String>> {  // I specified a typeCast for List<String>
            (it as? List<*>).asOptional().takeIfSome {
                isNotEmpty() && this[0] is String
            }.letSome { list ->
                @Suppress("UNCHECKED_CAST")
                list as List<String>
            }
        }
        val AMBIGUOUS_LIST by instance<List<*>> {  // I added an instance for the ambiguous list
            (it as? List<*>).asOptional().takeIfSome {
                isEmpty()
            }
        }
    }

    @Test
    fun testUCases() {
        val intList = listOf(1, 2, 3)
        val stringList = listOf("a", "b", "c")
        val emptyList = emptyList<Int>()
        val intListUnion = intList.wrapAs(ColorListCases.INTS)
        val stringListUnion = stringList.wrapAs(ColorListCases.STRINGS)
        val emptyListUnion = emptyList.wrapAs(ColorListCases.AMBIGUOUS_LIST)
    }

}