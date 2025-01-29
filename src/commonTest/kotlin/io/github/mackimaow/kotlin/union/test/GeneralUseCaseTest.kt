package io.github.mackimaow.kotlin.union.test
import io.github.mackimaow.kotlin.union.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/*
 Basic tests for Union
 */

class GeneralUseCaseTest {

    // some arbitrary classes for union
    data class Bird(val name: String, val age: Int)
    data class Zebra(val name: String)
    data class Giraffe(val name: String, val age: Int)
    data class Elephant(val name: String, val age: Int)

    // union defined
    object AnimalCases: MatchCases<AnimalCases>() {
        val BIRD by instance<Bird>()
        val ZEBRA by instance<Zebra>()
        val GIRAFFE by instance<Giraffe>()
        val ELEPHANT by instance<Elephant>()

        val BRENDON by obj("Brendon the Ape")
        val HENRY by obj("Henry the Mouse")
        val DOUGLAS by obj("Douglas the Lion")

        val BIRDS by instance<List<Bird>> {
            // toType to counter type erasure of List<T>
            (it as? List<*>).asOptional().takeIfSome {
                isNotEmpty() && all { item -> item is Bird }
            }.letSome { list ->
                @Suppress("UNCHECKED_CAST")
                list as List<Bird>
            }
        }
        val GIRAFFES by instance<List<Giraffe>> {
            // toType to counter type erasure of List<T>
            (it as? List<*>).asOptional().takeIfSome {
                isNotEmpty() && all { item -> item is Giraffe }
            }.letSome { list ->
                @Suppress("UNCHECKED_CAST")
                list as List<Giraffe>
            }
        }
        val EMPTY_LIST by instance<List<*>> {
            // toType to counter type erasure of List<T>
            (it as? List<*>).asOptional()
        }
    }

    @Test
    fun testMap() {
        // animals in zoo
        val josh = Bird("Josh", 2)
        val joe = Bird("Joe", 3)
        val sue = Zebra("Sue")
        val tucker = Zebra("Tucker")
        val charles = Giraffe("Charles", 6)
        val mary = Giraffe("Mary", 7)
        val brutus = Elephant("Brutus", 8)
        val john = Elephant("John", 9)

        val testCases = mapOf(
            josh        to 15,
            joe         to 3,
            sue         to 8,  // has same age as Brutus
            tucker      to 9,  // has same age as John
            charles     to 6,
            mary        to 10,
            brutus      to 8,
            john        to 9,
            listOf<Bird>()          to 10,
            listOf(josh, joe)       to 5,
            listOf<Giraffe>()       to 10,
            listOf(charles, mary)   to 15,
            "Brendon the Ape"   to 10,
            "Henry the Mouse"   to 4,
            "Douglas the Lion"  to 10,
        )

        val animalPen = object {
            var numberOfAnimalsInPen = 0
        }

        fun tendAnimalAndGuessAge(animal: Union<AnimalCases>): Int {
            // pretend zookeeper is guessing here
            return with(AnimalCases) {
                animal.morph {
                    runWhen(BIRD) {
                        animalPen.numberOfAnimalsInPen += 1
                    }
                    runWhen(BIRD) {
                        if (name == "Josh")
                            return@runWhen  // not sure about josh, it's not known to me
                        return@morph age
                    }
                    runWhen(BIRD) {
                        if (this == josh)
                            return@morph 15 // He's 15 I think
                    }
                    changeWhen(ZEBRA) { // transform holder
                        if (name == "Sue")
                            wrap(brutus).getOrThrow() // Same as Brutus
                        else
                            wrap(john).getOrThrow()  // Same as John
                    }
                    runWhen(GIRAFFE) {
                        if (name == "Mary")
                            animalPen.numberOfAnimalsInPen += 1
                    }
                    runWhen(GIRAFFE) {
                        if (name == "Mary")
                            return@runWhen  // She wants to be older than she seems (so I will lie about this)
                        return@morph age
                    }
                    runWhen(ELEPHANT) {  // Brutus and Sue are in a pen
                        if (this == brutus)
                            animalPen.numberOfAnimalsInPen += 1
                    }
                    runWhen(ELEPHANT) {
                        return@morph age
                    }
                    runWhen(BIRDS) {
                        return@morph sumOf { it.age }  // I am going to sum ages of both for birds
                    }
                    runWhen(GIRAFFES) {
                        return@morph 2 + sumOf { it.age } // I am going to sum ages of both for giraffes, then add 2
                    }
                    runWhen(HENRY) {
                        return@morph 4
                    }
                    changeWhen(BRENDON) {
                        HENRY.wrap()  // Oh wait, it's the same as henry!
                    }
                    runWhen(HENRY) {
                        return@runWhen  // Uhh, actually I don't know Brendon's age
                    }
                    return@morph 10
                }
            }
        }

        testCases.map { (animal, caseAnswer) ->
            AnimalCases.wrap(animal).getOrThrow() to caseAnswer
        }.forEach { (animal, caseAnswer) ->
            val age = tendAnimalAndGuessAge(animal)
            assertEquals(caseAnswer, age, "Failed using otherwise on animal $animal")
        }

        assertEquals(
            5,
            animalPen.numberOfAnimalsInPen,
            "Failed to match number of animals in their pen while thinking of ages"
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testCoroutines() = runTest {
        // animals in zoo
        val josh = Bird("Josh", 2)
        val joe = Bird("Joe", 3)
        val sue = Zebra("Sue")
        val tucker = Zebra("Tucker")
        val charles = Giraffe("Charles", 6)
        val mary = Giraffe("Mary", 7)
        val brutus = Elephant("Brutus", 8)
        val john = Elephant("John", 9)

        val testCases = mapOf(
            josh        to 15,
            joe         to 3,
            sue         to 8,  // has same age as Brutus
            tucker      to 9,  // has same age as John
            charles     to 6,
            mary        to 10,
            brutus      to 8,
            john        to 9,
            listOf<Bird>()          to 10,
            listOf(josh, joe)       to 5,
            listOf<Giraffe>()       to 10,
            listOf(charles, mary)   to 15,
            "Brendon the Ape"   to 10,
            "Henry the Mouse"   to 4,
            "Douglas the Lion"  to 10,
        )

        val animalPen1 = object {
            var numberOfAnimalsInPen = 0
        }

        suspend fun tendAnimalAndGuessAge(animal: Union<AnimalCases>): Int {
            // pretend zookeeper is guessing here
            return with(AnimalCases) {
                animal.morph {
                    runWhen(BIRD) {
                        delay(10)
                        animalPen1.numberOfAnimalsInPen += 1
                    }
                    runWhen(BIRD) {
                        if (name == "Josh")
                            return@runWhen  // not sure about josh, it's not known to me
                        return@morph age
                    }
                    runWhen(BIRD) {
                        if (this == josh)
                            return@morph 15 // He's 15 I think
                    }
                    changeWhen(ZEBRA) {
                        delay(10)// orm holder
                        if (name == "Sue")
                            wrap(brutus).getOrThrow()// Same as Brutus
                        else
                            wrap(john).getOrThrow()  // Same as John
                    }
                    runWhen(GIRAFFE) {
                        if (name == "Mary")
                            animalPen1.numberOfAnimalsInPen += 1
                    }
                    runWhen(GIRAFFE) {
                        if (name == "Mary")
                            return@runWhen  // She wants to be older than she seems (so I will lie about this)
                        return@morph age
                    }
                    runWhen(ELEPHANT) { // Brutus and Sue are in a pen
                        if (this == brutus)
                            animalPen1.numberOfAnimalsInPen += 1
                    }
                    runWhen(ELEPHANT) {
                        return@morph age
                    }
                    runWhen(BIRDS) {
                        return@morph sumOf { it.age }  // I am going to sum ages of both for birds
                    }
                    runWhen(GIRAFFES) {
                        return@morph 2 + sumOf { it.age } // I am going to sum ages of both for giraffes, then add 2
                    }
                    runWhen(HENRY) {
                        return@morph 4
                    }
                    changeWhen(BRENDON) {
                        HENRY.wrap()  // Oh wait, it's the same as henry!
                    }
                    runWhen(HENRY) {
                        return@runWhen  // Uhh, actually I don't know Brendon's age
                    }
                    return@morph 10
                }
            }
        }

        testCases.map { (animal, caseAnswer) ->
            AnimalCases.wrap(animal).getOrThrow() to caseAnswer
        }.forEach { (animal, caseAnswer) ->
            val age1 = tendAnimalAndGuessAge(animal)
            assertEquals(caseAnswer, age1, "Failed using otherwise on animal $animal")
        }


        assertEquals(
            5,
            animalPen1.numberOfAnimalsInPen,
            "Failed to match number of animals in their pen while thinking of ages (First Method)"
        )
    }

    object NumberCases: MatchCases<NumberCases>() {
        val INT by instance<Int>()
        val FLOAT by instance<Float>()
    }

    // Union of (Int | Float) | "red" | "green" | "blue"
    object ColorCases: MatchCases<ColorCases>() {
        val NUMBER by union(NumberCases)
        val RED by obj("red")
        val GREEN by obj("green")
        val BLUE by obj("blue")
    }

    private fun colorAsHexWithAlpha(color: Union<ColorCases>): Union<ColorCases> {
        return with(ColorCases) { with(NumberCases) {
            color.morphSelf {
                changeByMorphingCase(NUMBER) {
                    changeWhen(INT) {
                        or(0xFF000000u.toInt()).wrapAs(INT)
                    }
                    changeWhen(FLOAT) {
                        val intHex: Int = toInt()
                        intHex.wrapAs(INT)
                    }
                }
                changeWhen(RED) {
                    0xFF0000.wrapAs(INT).wrapAs(NUMBER)
                }
                changeWhen(GREEN) {
                    0x00FF00.wrapAs(INT).wrapAs(NUMBER)
                }
                changeWhen(BLUE) {
                    0x0000FF.wrapAs(INT).wrapAs(NUMBER)
                }
                changeByMorphingCase(NUMBER) {
                    changeWhen(INT) {
                        or(0xFF000000u.toInt()).wrapAs(INT)
                    }
                }
            }
        }
    } }

    @Test
    fun testNestedUnion() {
        val testCases: Map<*, Int> = mapOf(
            12.0f to (12.0f.toInt() or 0xFF000000u.toInt()),
            100.0f to (100.0f.toInt() or 0xFF000000u.toInt()),
            0x123456 to (0x123456 or 0xFF000000u.toInt()),
            0x654321 to (0x654321 or 0xFF000000u.toInt()),
            "red" to (0xFF0000 or 0xFF000000u.toInt()),
            "green" to (0x00FF00 or 0xFF000000u.toInt()),
            "blue" to (0x0000FF or 0xFF000000u.toInt())
        )

        testCases.forEach {(colorUnwrapped, answer) ->
            val colorUnwrappedNew = if (colorUnwrapped is Float || colorUnwrapped is Int)
                colorUnwrapped.wrapAs(NumberCases).getOrThrow()
            else
                colorUnwrapped
            val color =  colorUnwrappedNew.wrapAs(ColorCases).getOrThrow()
            val result = colorAsHexWithAlpha(color)
            val unwrappedResult = result
                .unwrapFrom(ColorCases.NUMBER).toNullable()
                ?.unwrapFrom(NumberCases.INT)?.toNullable()!!
            assertEquals(
                answer,
                unwrappedResult,
                "For $color, I got $unwrappedResult when it should be $answer"
            )
        }
    }

    @Test
    fun testOrdinalsAndNames() {
        assertEquals(NumberCases.INT.ordinal, 0)
        assertEquals(NumberCases.INT.name, "INT")
        assertEquals(NumberCases.FLOAT.ordinal, 1)
        assertEquals(NumberCases.FLOAT.name, "FLOAT")

        assertEquals(ColorCases.NUMBER.ordinal, 0)
        assertEquals(ColorCases.NUMBER.name, "NUMBER")
        assertEquals(ColorCases.RED.ordinal, 1)
        assertEquals(ColorCases.RED.name, "RED")
        assertEquals(ColorCases.GREEN.ordinal, 2)
        assertEquals(ColorCases.GREEN.name, "GREEN")
        assertEquals(ColorCases.BLUE.ordinal, 3)
        assertEquals(ColorCases.BLUE.name, "BLUE")
    }

    object NestedCases: MatchCases<NestedCases>() {
        val COLOR by union(RecursiveCases)
    }

    object OtherCases: MatchCases<OtherCases>() {
        val REC by union(NestedCases)
    }

    object RecursiveCases: MatchCases<RecursiveCases>() {
        val REC: Int = 0
        fun test() {
            // this is not an actual use case, but it needs to be tested
            union(OtherCases).provideDelegate(this, RecursiveCases::REC /* any property would do for this test */)
        }
    }

    @Test
    fun recursive() {
        assertFails {
            RecursiveCases.test()
        }
    }
}