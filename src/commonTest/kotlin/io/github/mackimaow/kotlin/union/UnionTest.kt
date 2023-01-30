package io.github.mackimaow.kotlin.union
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*


class UnionTest {

    // some arbitrary classes for union
    data class Bird(val name: String, val age: Int)
    data class Zebra(val name: String)
    data class Giraffe(val name: String, val age: Int)
    data class Elephant(val name: String, val age: Int)
    data class Penguin(val name: String, val age: Int)

    // union defined
    object Animal: UnionOptions<Animal>({ Animal }) {
        val BIRD = option<Bird>()
        val ZEBRA = option<Zebra>()
        val GIRAFFE = option<Giraffe>()
        val ELEPHANT = option<Elephant>()
        val BIRDS = option<List<Bird>> {  // discriminator to counter type erasure of List<T>
            it is List<*> && (it.isEmpty() || it[0] is Bird)
        }
        val GIRAFFES = option<List<Giraffe>> {  // discriminator to counter type erasure of List<T>
            it is List<*> && (it.isNotEmpty() && it[0] is Giraffe)
        }
        val BRENDON = literal("Brendon the Ape")
        val HENRY = literal("Henry the Mouse")
        val DOUGLAS = literal("Douglas the Lion")
    }

    @Test
    fun testUnion() {

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
            listOf<Bird>()          to 0,
            listOf(josh, joe)       to 5,
            listOf<Giraffe>()       to 0,
            listOf(charles, mary)   to 15,
            "Brendon the Ape"   to 10,
            "Henry the Mouse"   to 4,
            "Douglas the Lion"  to 10,
        )

        val animalPen1 = object {
            var numberOfAnimalsInPen = 0
        }
        val animalPen2 = object {
            var numberOfAnimalsInPen = 0
        }

        fun tendAnimalAndGuessAge(animal: Union<Animal>): Int {
            // pretend zoo keeper is guessing here
            return animal.map {
                execute(Animal.BIRD) {
                    animalPen1.numberOfAnimalsInPen += 1
                }
                accept(Animal.BIRD) {
                    if (it.name == "Josh")
                        Continue  // not sure about josh, it's not known to me
                    it.age
                }
                accept(Animal.BIRD, josh) {
                    15 // He's 15 I think
                }
                change(Animal.ZEBRA) { // transform holder
                    if (it.name == "Sue")
                        Animal.wrap(brutus)!!// Same as Brutus
                    else
                        Animal.wrap(john)!!  // Same as John
                }
                execute(Animal.GIRAFFE) {
                    if (it.name == "Mary")
                        animalPen1.numberOfAnimalsInPen += 1
                }
                accept(Animal.GIRAFFE) {
                    if (it.name == "Mary")
                        Break  // She wants to be older than she seems (so I will lie about this)
                    it.age
                }
                execute(Animal.ELEPHANT, brutus) {  // Brutus and Sue are in a pen
                    animalPen1.numberOfAnimalsInPen += 1
                }
                accept(Animal.ELEPHANT) {
                    it.age
                }
                accept(Animal.BIRDS) {
                    it.sumOf { it.age }  // I am going to sum ages of both for birds
                }
                accept(Animal.GIRAFFES) {
                    2 + it.sumOf { it.age } // I am going to sum ages of both for giraffes, then add 2
                }
                accept(Animal.BRENDON) {
                    Continue  // Hmm, not sure
                }
                accept(Animal.HENRY) {
                    4
                }
                change(Animal.BRENDON) {
                    Animal.HENRY.wrap()  // Oh wait, it's the same as henry!
                }
                accept(Animal.HENRY) {
                    Break  // Uhh, actually I don't know Brendon's age
                }
                otherwise {
                    10  // not sure, so I am going to assume 10 for some reason
                }
            }
        }

        fun tendAnimalAndGuessAgeUsingDefault(animal: Union<Animal>): Int {
            // pretend zoo keeper is guessing here
            return animal.map(10) {
                execute(Animal.BIRD) {
                    animalPen2.numberOfAnimalsInPen += 1
                }
                accept(Animal.BIRD) {
                    if (it.name == "Josh")
                        Continue  // not sure about josh, it's not known to me
                    it.age
                }
                accept(Animal.BIRD, josh) {
                    15 // He's 15 I think
                }
                change(Animal.ZEBRA) { // change what the union is
                    if (it.name == "Sue")
                        Animal.wrap(brutus)!!// Same as Brutus
                    else
                        Animal.wrap(john)!!  // Same as John
                }
                execute(Animal.GIRAFFE) {
                    if (it.name == "Mary")
                        animalPen2.numberOfAnimalsInPen += 1
                }
                accept(Animal.GIRAFFE) {
                    if (it.name == "Mary")
                        Break  // She wants to be older than she seems (so I will lie about this)
                    it.age
                }
                execute(Animal.ELEPHANT, brutus) {  // Brutus and Sue are in a pen
                    animalPen2.numberOfAnimalsInPen += 1
                }
                accept(Animal.ELEPHANT) {
                    it.age
                }
                accept(Animal.BIRDS) {
                    it.sumOf { it.age }  // I am going to sum ages of both for birds
                }
                accept(Animal.GIRAFFES) {
                    2 + it.sumOf { it.age } // I am going to sum ages of both for giraffes, then add 2
                }
                accept(Animal.BRENDON) {
                    Continue  // Hmm, not sure
                }
                accept(Animal.HENRY) {
                    4
                }
                change(Animal.BRENDON) {
                    Animal.HENRY.wrap()  // Oh wait, it's the same as henry!
                }
                accept(Animal.HENRY) {
                    Break  // Uhh, actually I don't know Brendon's age
                }
            }
        }

        testCases.map { (animal, caseAnswer) ->
            Animal.wrap(animal)!! to caseAnswer
        }.forEach { (animal, caseAnswer) ->
            val age1 = tendAnimalAndGuessAge(animal)
            assertEquals(caseAnswer, age1, "Failed using otherwise on animal $animal")
            val age2 = tendAnimalAndGuessAgeUsingDefault(animal)
            assertEquals(caseAnswer, age2, "Failed using default on animal $animal")
        }

        assertEquals(
            5,
            animalPen1.numberOfAnimalsInPen,
            "Failed to match number of animals in their pen while thinking of ages (First Method)"
        )

        assertEquals(
            5,
            animalPen2.numberOfAnimalsInPen,
            "Failed to match number of animals in their pen while thinking of ages (Second Method)"
        )
    }

    @Test
    fun testAddOptionsToExisting() {
        assertFailsWith<WrongUnionOptionsType> {
            val animalExtended = object: UnionOptions<Animal>({ Animal }) {
                val ALEX = literal("Alex the Penguin") // using literal
            }
            println(
                "I tried to extend union $animalExtended by adding"+
                        " ${animalExtended.ALEX} mischievously!"
            )
        }
        assertFailsWith<WrongUnionOptionsType> {
            val animalExtended = object: UnionOptions<Animal>({ Animal }) {
                val ALEX = option<Penguin>()  // using option
            }
            println(
                "I tried to extend union $animalExtended by adding"+
                        " ${animalExtended.ALEX} mischievously!"
            )
        }
    }

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
            listOf<Bird>()          to 0,
            listOf(josh, joe)       to 5,
            listOf<Giraffe>()       to 0,
            listOf(charles, mary)   to 15,
            "Brendon the Ape"   to 10,
            "Henry the Mouse"   to 4,
            "Douglas the Lion"  to 10,
        )

        val animalPen1 = object {
            var numberOfAnimalsInPen = 0
        }

        suspend fun tendAnimalAndGuessAge(animal: Union<Animal>): Int {
            // pretend zoo keeper is guessing here
            return animal.map {
                execute(Animal.BIRD) {
                    delay(10) // tending
                    animalPen1.numberOfAnimalsInPen += 1
                }
                accept(Animal.BIRD) {
                    delay(10) // thinking
                    if (it.name == "Josh")
                        Continue  // not sure about josh, it's not known to me
                    it.age
                }
                accept(Animal.BIRD, josh) {
                    delay(10) // thinking
                    15 // He's 15 I think
                }
                change(Animal.ZEBRA) { // transform holder
                    delay(10) // thinking
                    if (it.name == "Sue")
                        Animal.wrap(brutus)!!// Same as Brutus
                    else
                        Animal.wrap(john)!!  // Same as John
                }
                execute(Animal.GIRAFFE) {
                    delay(10) // tending
                    if (it.name == "Mary")
                        animalPen1.numberOfAnimalsInPen += 1
                }
                accept(Animal.GIRAFFE) {
                    delay(10) // thinking
                    if (it.name == "Mary")
                        Break  // She wants to be older than she seems (so I will lie about this)
                    it.age
                }
                execute(Animal.ELEPHANT, brutus) {  // Brutus and Sue are in a pen
                    delay(10) // tending
                    animalPen1.numberOfAnimalsInPen += 1
                }
                accept(Animal.ELEPHANT) {
                    delay(10) // thinking
                    it.age
                }
                accept(Animal.BIRDS) {
                    delay(10) // thinking
                    it.sumOf { it.age }  // I am going to sum ages of both for birds
                }
                accept(Animal.GIRAFFES) {
                    delay(10) // thinking
                    2 + it.sumOf { it.age } // I am going to sum ages of both for giraffes, then add 2
                }
                accept(Animal.BRENDON) {
                    delay(10) // thinking
                    Continue  // Hmm, not sure
                }
                accept(Animal.HENRY) {
                    delay(10) // thinking
                    4
                }
                change(Animal.BRENDON) {
                    delay(10) // thinking
                    Animal.HENRY.wrap()  // Oh wait, it's the same as henry!
                }
                accept(Animal.HENRY) {
                    delay(10) // thinking
                    Break  // Uhh, actually I don't know Brendon's age
                }
                otherwise {
                    delay(10) // thinking
                    10  // not sure, so I am going to assume 10 for some reason
                }
            }
        }

        testCases.map { (animal, caseAnswer) ->
            Animal.wrap(animal)!! to caseAnswer
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
}