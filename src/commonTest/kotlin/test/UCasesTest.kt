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
        val strWrapped = RandomCases.wrap(str)!!
        strWrapped.let {
            val itUnwrapped = RandomCases.STRING.unwrap(it)!!
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
        assertTrue(valueOptional1 != null)
        valueOptional1.let {
            val actualVirus = it.unwrapFrom(SmallCases.VIRUS)!!
            assertEquals(actualVirus, virus)
        }

        val valueOptional2 = DiscernRandomCases.VIRUS.getValueIfMatchesCase(bacteriaWrapped)
        assertEquals(valueOptional2, null)
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
        assertTrue(valueOptional1 != null)
        assertEquals(valueOptional1.unwrapFrom(DiscernRandomCases.PERCENT)!!, value)

        val badValue = 101
        val badValueOptional = badValue.wrapAs(DiscernRandomCases.PERCENT)
        val badValueOptional2 = DiscernRandomCases.PERCENT.wrap(badValue)
        assertEquals(badValueOptional, badValueOptional2)
        assertEquals(badValueOptional, null)

        assertTrue(value.canWrapAs(RandomCases))
        assertFalse(badValue.canWrapAs(RandomCases))
    }

    @Test
    fun wrap() {
        val virus = Virus(true)
        val virusWrapped = SmallCases.VIRUS.wrap(virus)
        val virusWrappedTwice = RandomCases.wrap(virusWrapped)
        assertTrue(virusWrappedTwice != null)

        val catWrapped = RandomCases.wrap(Cat)
        assertTrue(catWrapped != null)

        val bacteria = Bacteria("Green")
        val bacteriaWrapped = SmallCases.BACTERIA.wrap(bacteria)
        val bacteriaWrappedTwice = RandomCases.wrap(bacteriaWrapped)
        assertTrue(bacteriaWrappedTwice != null)

        val catWrappedTwice = RandomCases.wrap(OrganismCases.CAT.wrap())
        assertTrue(catWrappedTwice != null)

        // wrapAs
        val virusWrappedAs = virus.wrapAs(SmallCases)!!
        virusWrappedAs.let {
            val itUnwrapped = SmallCases.VIRUS.unwrap(it)!!
            assertEquals(itUnwrapped, virus)
        }

        val virusWrappedTwiceAs = virusWrappedAs.wrapAs(RandomCases.VIRUS)!!
        virusWrappedTwiceAs.let {
            val itUnwrapped = RandomCases.VIRUS.unwrap(it)!!
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
        assertEquals(RandomCases2.wrap(virus), null)
        RandomCases3.wrap(virus)!!.let {
            val itUnwrapped = RandomCases3.MICROSCOPIC.unwrap(it)!!
            val itUnwrapped2 = SmallCases.VIRUS.unwrap(itUnwrapped)!!
            assertEquals(itUnwrapped2, virus)
        }
    }

    object RandomCases4: MatchCases<RandomCases4>() {
        val DOGS by instanceWhen<List<Dog>>(
            toType = {
                if (it is List<*> && it.isNotEmpty() && it.all { item ->  item is Dog }) {
                    @Suppress("UNCHECKED_CAST")
                    (it as List<Dog>)
                } else {
                    null
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
        assertTrue(dogsWrapped != null)
        dogsWrapped.let {
            val itUnwrapped = RandomCases4.DOGS.unwrap(it)!!
            assertEquals(itUnwrapped, dogs)
        }

        val dogs2 = listOf(Dog(true))
        val dogsWrapped2 = RandomCases4.wrap(dogs2)
        assertFalse(dogsWrapped2 != null)
    }

    @Test
    fun unwrapToAny() {
        val virus = Virus(true)
        val virusWrapped = SmallCases.VIRUS.wrap(virus)
        val virusWrappedTwiceOpt = RandomCases.VIRUS.wrap(virusWrapped)
        assertTrue(virusWrappedTwiceOpt != null)

        val virusWrappedTwiceUnwrapped = virusWrappedTwiceOpt.unwrap()
        assertEquals(virusWrappedTwiceUnwrapped, virus)

        val virusOpt1 = SmallCases.VIRUS.unwrap(virusWrapped)
        val virusOpt2 = virusWrapped.unwrapFrom(SmallCases.VIRUS)
        assertEquals(virusOpt1, virusOpt2)
        assertTrue(virusOpt1 != null)
        assertEquals(virusOpt1, virus)

        val bacteria = Bacteria("Green")
        val bacteriaWrapped = SmallCases.BACTERIA.wrap(bacteria)!!
        val virusOpt3 = SmallCases.VIRUS.unwrap(bacteriaWrapped)
        val virusOpt4 = bacteriaWrapped.unwrapFrom(SmallCases.VIRUS)
        assertEquals(virusOpt3, virusOpt4)
        assertEquals(virusOpt3, null)

        val bacteriaOpt1 = SmallCases.BACTERIA.unwrap(bacteriaWrapped)
        val bacteriaOpt2 = bacteriaWrapped.unwrapFrom(SmallCases.BACTERIA)
        assertEquals(bacteriaOpt1, bacteriaOpt2)
        assertTrue(bacteriaOpt1 != null)
        assertEquals(bacteriaOpt1, bacteria)
    }
}