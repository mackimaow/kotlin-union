package io.github.mackimaow.kotlin.union.test

import kotlin.test.Test
import io.github.mackimaow.kotlin.union.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodeExample1Test {
    object ColorCases: MatchCases<ColorCases>() {
        val HEX by union(JsNumberCases)
        val RED by obj("red")
        val GREEN by obj("green")
        val BLUE by obj("blue")
    }

    // the typescript "number" type can be translated
    // into kotlin as Union<CasesJSNumber>:
    object JsNumberCases: MatchCases<JsNumberCases>() {
        val INT by instance<Int>()
        val FLOAT by instance<Float>()
    }
}



class CodeExample2Test {
    object JsNumberCases: MatchCases<JsNumberCases>() {
        val INT by instance<Int>()
        val FLOAT by instance<Float>()
    }

    object ColorCases: MatchCases<ColorCases>() {
        val HEX by case.number() // using the custom case
        val RED by obj("red")
        val GREEN by obj("green")
        val BLUE by obj("blue")
    }

    companion object {
        // custom case jsNumber():
        fun <CS: UCases<CS>> UCaseSupplier<CS>.number() = union(JsNumberCases)
    }
}

class CodeExample3Test() {
    object ColorCases: MatchCases<ColorCases>() {
        val HEX by ColorCases.union(JsNumberCases)
        val RED by ColorCases.obj("red")
        val GREEN by ColorCases.obj("green")
        val BLUE by ColorCases.obj("blue")
    }

    // the typescript "number" type can be translated
    // into kotlin as Union<CasesJSNumber>:
    object JsNumberCases: MatchCases<JsNumberCases>() {
        val INT by instance<Int>()
        val FLOAT by instance<Float>()
    }

    fun getColor() = ColorCases.RED.wrap()

    @Test
    fun segment1() {
        val cases: List<UCase<ColorCases, *>> = ColorCases.cases
        val nameToCase: Map<String, UCase<ColorCases, *>> = ColorCases.nameToCase

        val redCase: UCase<ColorCases, *> = ColorCases.RED
        assertEquals(redCase.name, "RED") // "RED"
        assertEquals(redCase.ordinal, 1) // 1
    }

    @Test
    fun segment2() {
        // --- Creating unions by wrapping ---

        // Wrapping directly on case assures the type of union:
        val redColor: Union<ColorCases> = ColorCases.RED.wrap()

        // Not wrapping directly gives you a nullable Union<ColorCases>.
        // These will have a type of Union<ColorCases>:
        val greenColor: Union<ColorCases>? = ColorCases.wrap("green")
        assertTrue(greenColor != null)
        val blueColor: Union<ColorCases>? = "blue".wrapAs(ColorCases)
        assertTrue(blueColor != null)

        // These will have a type of Optional.None because it doesn't match any case:
        val yellowColor: Union<ColorCases>? = "yellow".wrapAs(ColorCases)
        assertEquals(yellowColor, null)

        // Creating unions by double wrapping:
        val intNumber: Union<JsNumberCases> = 0xFF0000.wrapAs(JsNumberCases.INT)
        val intColor: Union<ColorCases> = intNumber.wrapAs(ColorCases.HEX)

        // Creating a union directly (without double wrapping) from a float:
        val floatColor: Union<ColorCases>? = 0.5f.wrapAs(ColorCases)
        assertTrue(floatColor != null)
    }

    @Test
    fun segment3() {
        // Suppose some function getColor() exists
        val myColor: Union<ColorCases> = getColor()
        val unwrappedColorValue: Any? = myColor.unwrap()
    }

    @Test
    fun segment4() {
        val colorString = "red"
        assertTrue(ColorCases.canWrap(colorString)) // true

        val notAColorString = "strawberry"
        assertFalse(ColorCases.canWrap(notAColorString)) // false
    }

    @Test
    fun segment5() {
        // for the sake of the example, getColor() returns a Union<ColorCases>
        val color: Union<ColorCases> = getColor()

        color.alsoWhen(ColorCases.HEX) { hexColor: Union<JsNumberCases> ->
            hexColor.alsoWhen(JsNumberCases.INT) { intColor: Int ->
                println("I'm an integer color: $intColor")
            }.alsoWhen(JsNumberCases.FLOAT) { floatColor: Float ->
                println("I'm a float color: $floatColor")
            }
        }.alsoWhen(ColorCases.RED) { red: String ->
            println("I'm a $red color")
        }.alsoWhen(ColorCases.GREEN) { green: String ->
            println("I'm a $green color")
        }.alsoWhen(ColorCases.BLUE) { blue: String ->
            println("I'm a $blue color")
        }
    }

    @Test
    fun segment6() {
        val color: Union<ColorCases> = getColor()

        with(ColorCases) {
            with(JsNumberCases) {
                color.alsoWhen(HEX) { hexColor ->
                    hexColor.alsoWhen(INT) {
                        println("I'm an integer color: $it")
                    }.alsoWhen(FLOAT) {
                        println("I'm a float color: $it")
                    }
                }.alsoWhen(RED) {
                    println("I'm a $it color")
                }.alsoWhen(GREEN) {
                    println("I'm a $it color")
                }.alsoWhen(BLUE) {
                    println("I'm a $it color")
                }
            }
        }
    }

    @Test
    fun segment7() {
        val myColor: Union<ColorCases> = getColor()

        // morph color union to boolean
        val isFavoriteColor: Boolean = myColor.morph {
            // I have access to the current value of the union, which
            // --at the moment-- is equal to myColor
            println(current == myColor) // true
            assertEquals(current, myColor)

            changeByMorph(ColorCases.HEX) {
                changeWhen(JsNumberCases.FLOAT) {
                    val intHex: Int = (this * 255).toInt()
                    JsNumberCases.INT.wrap(intHex)
                }
                runWhen(JsNumberCases.INT) {
                    if (this < 256)
                        ColorCases.BLUE.wrap()
                    else
                        return@morph false
                }!!
            }

            alsoWhen(ColorCases.GREEN) { green: String ->
                println("I don't like $green!")
            }

            runWhen(ColorCases.BLUE) {
                println("I love $this!")
                true
            } ?: false
        }
    }
}

class CodeExample4Test() {
    // Union of List<Float> | List<Int>
    object NumbersCases: MatchCases<NumbersCases>() {
        // add matcher to distinguish List<Float>
        val FLOATS by instance<List<Float>> { obj: Any? ->
            val list = obj as? List<*>
            if (list != null) {
                if (list.isNotEmpty() && list.all { item -> item is Float }) {
                    @Suppress("UNCHECKED_CAST")
                    val floatList = list as List<Float>
                    return@instance floatList
                }
            }
            return@instance null
        }

        // add matcher to distinguish List<Int>
        val INTS by instance<List<Int>> { obj: Any? ->
            val list = obj as? List<*>
            if (list != null) {
                if (list.isNotEmpty() && list.all { item -> item is Int }) {
                    @Suppress("UNCHECKED_CAST")
                    val intList = list as List<Int>
                    return@instance intList
                }
            }
            return@instance null
        }

        // add the ambiguous empty list case with its own matcher
        val AMBIGUOUS_LIST by instance<List<*>> { obj: Any? ->
            val list = obj as? List<*>
            if (list != null)
                if (list.isEmpty())
                    return@instance list
            return@instance null
        }
    }
}

class CodeExample5Test() {

    // Union of List<Float> | List<Int>
    object NumbersCases: DiscernCases<NumbersCases>() {
        // add differentiator to distinguish List<Float>
        val FLOATS by instance<List<Float>> { obj: Any? ->
            val list = obj as? List<*>
            if (list != null) {
                // only now have to check if the first element is a float
                if (list.isNotEmpty() && list[0] is Float) {
                    @Suppress("UNCHECKED_CAST")
                    val floatList = list as List<Float>
                    return@instance floatList
                }
            }
            return@instance null
        }

        // add differentiator to distinguish List<Int>
        val INTS by instance<List<Int>> { obj: Any? ->
            val list = obj as? List<*>
            if (list != null) {
                // only now have to check if the first element is a int
                if (list.isNotEmpty() && list[0] is Int) {
                    @Suppress("UNCHECKED_CAST")
                    val intList = list as List<Int>
                    return@instance intList
                }
            }
            return@instance null
        }

        // add the ambiguous empty list case with its own differentiator
        val AMBIGUOUS_LIST by instance<List<*>> { obj: Any? ->
            val list = obj as? List<*>
            if (list != null)
                if (list.isEmpty())
                    return@instance list
            return@instance null
        }
    }
}