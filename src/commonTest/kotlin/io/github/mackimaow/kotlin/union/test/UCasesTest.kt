package io.github.mackimaow.kotlin.union.test

import io.github.mackimaow.kotlin.union.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UCasesTest {
    interface Small {
        val isAlive: Boolean
    }
    data class Bacteria(val color: String): Small {
        override val isAlive: Boolean = true
    }
    data class Virus(val isDeadly: Boolean): Small {
        override val isAlive: Boolean = false
    }

    object SmallCases: MatchCases<SmallCases>() {
        val BACTERIA by instanceWhen<Bacteria> {
            it.color.isNotEmpty()
        }
        val VIRUS by instance<Virus>()
    }

    interface Fungus {
        val isMushroom: Boolean
    }
    data object Mushroom: Fungus {
        override val isMushroom: Boolean = true
    }
    data class Mold(val color: String): Fungus {
        override val isMushroom: Boolean = false
    }

    object FungusCases: MatchCases<FungusCases>() {
        val MUSHROOM by obj(Mushroom)
        val MOLD by instanceWhen<Mold> {
            it.color.isNotEmpty()
        }
    }

    interface Plant {
        val isTree: Boolean
    }
    data object Tree: Plant {
        override val isTree: Boolean = true
    }
    data class Flower(val color: String): Plant {
        override val isTree: Boolean = false
    }
    interface Animal {
        val species: String
    }
    data object Cat: Animal {
        override val species: String = "Cat"
    }
    data class Dog(val isPuppy: Boolean): Animal {
        override val species: String = "Dog"
    }

    object OrganismCases: MatchCases<OrganismCases>() {
        val MICROSCOPIC by unionWhen(SmallCases) {
            it.morph {
                runWhen(SmallCases.BACTERIA) {
                    return@morph true
                }
                false
            }
        }
        val FUNGUS by union(FungusCases)
        val TREE by obj(Tree)
        val FLOWER by instanceWhen<Flower> {
            it.color.isNotEmpty()
        }
        val DOG by instance<Dog>()
        val CAT by obj(Cat)
    }

    object DiscernRandomCases: DiscernCases<DiscernRandomCases>() {
        val STRING by instance<String>()
        val PERCENT by instanceWhen<Int> {
            it in 0..100
        }
        val ORGANISM by union(OrganismCases)
        val VIRUS by unionWhen(SmallCases) {
            it.morph {
                runWhen(SmallCases.VIRUS) {
                    return@morph true
                }
                false
            }
        }
        val CAT by obj(OrganismCases.CAT)
    }

    object RandomCases: MatchCases<RandomCases>() {
        val STRING by instance<String>()
        val PERCENT by instanceWhen<Int> {
            it in 0..100
        }
        val ORGANISM by union(OrganismCases)
        val VIRUS by unionWhen(SmallCases) {
            it.morph {
                runWhen(SmallCases.VIRUS) {
                    return@morph true
                }
                false
            }
        }
        val CAT by obj(Cat)
    }

    @Test
    fun wrapInstance() {
        val str = "Hello World"
        val strWrapped = RandomCases.wrap(str).getOrThrow()
        strWrapped.let {
            val itUnwrapped = RandomCases.STRING.unwrap(it).getOrThrow()
            assertEquals(itUnwrapped, str)
        }
    }

    @Test
    fun testGetValueIfMatchesCase() {
        val virus = Virus(true)
        val virusWrapped = SmallCases.VIRUS.wrap(virus)
        val bacteria = Bacteria("Green")
        val bacteriaWrapped = SmallCases.BACTERIA.wrap(bacteria)

        val valueOptional1 = DiscernRandomCases.VIRUS.getValueIfMatchesCase(virusWrapped)
        assertTrue(valueOptional1.isSome)
        valueOptional1.letSome {
            val actualVirus = it.unwrapFrom(SmallCases.VIRUS).getOrThrow()
            assertEquals(actualVirus, virus)
        }

        val valueOptional2 = DiscernRandomCases.VIRUS.getValueIfMatchesCase(bacteriaWrapped)
        assertTrue(!valueOptional2.isSome)
    }

    @Test
    fun matchCasesCanWrap() {
        val virus = Virus(true)

        assertTrue(SmallCases.canWrap(virus))
        assertFalse(SmallCases.canWrap(Cat))
    }


    @Test
    fun testSpecificInstance() {
        val value = 12
        val valueOptional1 = value.wrapAs(DiscernRandomCases.PERCENT)
        val valueOptional2 = DiscernRandomCases.PERCENT.wrap(value)
        assertEquals(valueOptional1, valueOptional2)
        assertTrue(valueOptional1.isSome)
        valueOptional1.letSome {
            assertEquals(it.unwrapFrom(DiscernRandomCases.PERCENT).getOrThrow(), value)
        }

        val badValue = 101
        val badValueOptional = badValue.wrapAs(DiscernRandomCases.PERCENT)
        val badValueOptional2 = DiscernRandomCases.PERCENT.wrap(badValue)
        assertEquals(badValueOptional, badValueOptional2)
        assertTrue(!badValueOptional.isSome)

        assertTrue(value.canWrapAs(RandomCases))
        assertFalse(badValue.canWrapAs(RandomCases))
    }

    @Test
    fun wrap() {
        val virus = Virus(true)
        val virusWrapped = SmallCases.VIRUS.wrap(virus)
        val virusWrappedTwice = RandomCases.wrap(virusWrapped)
        assertTrue(virusWrappedTwice.isSome)

        val catWrapped = RandomCases.wrap(Cat)
        assertTrue(catWrapped.isSome)

        val bacteria = Bacteria("Green")
        val bacteriaWrapped = SmallCases.BACTERIA.wrap(bacteria)
        val bacteriaWrappedTwice = RandomCases.wrap(bacteriaWrapped)
        assertFalse(bacteriaWrappedTwice.isSome)

        val catWrappedTwice = RandomCases.wrap(OrganismCases.CAT.wrap())
        assertTrue(catWrappedTwice.isSome)

        // wrapAs
        val virusWrappedAs = virus.wrapAs(SmallCases).getOrThrow()
        virusWrappedAs.let {
            val itUnwrapped = SmallCases.VIRUS.unwrap(it).getOrThrow()
            assertEquals(itUnwrapped, virus)
        }

        val virusWrappedTwiceAs = virusWrappedAs.wrapAs(RandomCases.VIRUS).getOrThrow()
        virusWrappedTwiceAs.let {
            val itUnwrapped = RandomCases.VIRUS.unwrap(it).getOrThrow()
            assertEquals(itUnwrapped, virusWrappedAs)
        }
    }

    @Test
    fun testOrdinalsNamesAndToString() {
        // for discern cases
        val discernCases = DiscernRandomCases.cases
        val expectedNames = listOf("STRING", "PERCENT", "ORGANISM", "VIRUS", "CAT")
        val expectedOrdinals = listOf(0, 1, 2, 3, 4)
        val expectedDiscernCasesToString = listOf(
            "${DiscernRandomCases::class.simpleName}.InstanceCase(name=STRING, ordinal=0)",
            "${DiscernRandomCases::class.simpleName}.SpecificInstanceCase(name=PERCENT, ordinal=1)",
            "${DiscernRandomCases::class.simpleName}.UnionCase(name=ORGANISM, ordinal=2)",
            "${DiscernRandomCases::class.simpleName}.SpecificUnionCase(name=VIRUS, ordinal=3)",
            "${DiscernRandomCases::class.simpleName}.ObjectCase(name=CAT, ordinal=4)"
        )
        for (i in discernCases.indices) {
            val case = discernCases[i]
            assertEquals(expectedNames[i], case.name)
            assertEquals(expectedOrdinals[i], case.ordinal)
            assertEquals(expectedDiscernCasesToString[i], case.toString())
        }

        // for match cases
        val matchCases = RandomCases.cases
        val expectedMatchCasesToString = listOf(
            "${RandomCases::class.simpleName}.InstanceCase(name=STRING, ordinal=0)",
            "${RandomCases::class.simpleName}.SpecificInstanceCase(name=PERCENT, ordinal=1)",
            "${RandomCases::class.simpleName}.UnionCase(name=ORGANISM, ordinal=2)",
            "${RandomCases::class.simpleName}.SpecificUnionCase(name=VIRUS, ordinal=3)",
            "${RandomCases::class.simpleName}.ObjectCase(name=CAT, ordinal=4)"
        )
        for (i in matchCases.indices) {
            val case = matchCases[i]
            assertEquals(expectedNames[i], case.name)
            assertEquals(expectedOrdinals[i], case.ordinal)
            assertEquals(expectedMatchCasesToString[i], case.toString())
        }
    }

    object RandomCases2: MatchCases<RandomCases2>() {
        val BACTERIA by unionWhen(SmallCases) {
            it.morph {
                runWhen(SmallCases.BACTERIA) {
                    return@morph true
                }
                false
            }
        }
        val BACTERIA2 by unionWhen(SmallCases) {
            it.morph {
                runWhen(SmallCases.BACTERIA) {
                    return@morph true
                }
                false
            }
        }
    }

    object RandomCases3: MatchCases<RandomCases3>() {
        val BACTERIA by unionWhen(SmallCases) {
            it.morph {
                runWhen(SmallCases.BACTERIA) {
                    return@morph true
                }
                false
            }
        }
        val MICROSCOPIC by union(SmallCases)
    }

    @Test
    fun moreWrapAs() {
        val virus = Virus(true)
        assertEquals(RandomCases2.wrap(virus), Optional.None)
        RandomCases3.wrap(virus).getOrThrow().let {
            val itUnwrapped = RandomCases3.MICROSCOPIC.unwrap(it).getOrThrow()
            val itUnwrapped2 = SmallCases.VIRUS.unwrap(itUnwrapped).getOrThrow()
            assertEquals(itUnwrapped2, virus)
        }
    }

    object RandomCases4: MatchCases<RandomCases4>() {
        val DOGS by instanceWhen<List<Dog>>(
            toType = {
                if (it is List<*> && it.isNotEmpty() && it.all { item ->  item is Dog }) {
                    @Suppress("UNCHECKED_CAST")
                    (it as List<Dog>).asSome()
                } else {
                    Optional.None
                }
            },
            isCase = { it: List<Dog> ->
                it.size > 1
            }
        )
        val CAT by obj(Cat)
    }

    @Test
    fun testInstanceWhenWithType() {
        val dogs = listOf(Dog(true), Dog(false))
        val dogsWrapped = RandomCases4.wrap(dogs)
        assertTrue(dogsWrapped.isSome)
        dogsWrapped.letSome {
            val itUnwrapped = RandomCases4.DOGS.unwrap(it).getOrThrow()
            assertEquals(itUnwrapped, dogs)
        }

        val dogs2 = listOf(Dog(true))
        val dogsWrapped2 = RandomCases4.wrap(dogs2)
        assertFalse(dogsWrapped2.isSome)
    }

    @Test
    fun unwrapToAny() {
        val virus = Virus(true)
        val virusWrapped = SmallCases.VIRUS.wrap(virus)
        val virusWrappedTwiceOpt = RandomCases.VIRUS.wrap(virusWrapped)
        assertTrue(virusWrappedTwiceOpt.isSome)
        val virusWrappedTwice = virusWrappedTwiceOpt.getOrThrow()

        val virusWrappedTwiceUnwrapped = virusWrappedTwice.unwrap()
        assertEquals(virusWrappedTwiceUnwrapped, virus)
    }
}