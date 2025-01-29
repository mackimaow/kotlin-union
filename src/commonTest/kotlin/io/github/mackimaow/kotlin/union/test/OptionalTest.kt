package io.github.mackimaow.kotlin.union.test

import io.github.mackimaow.kotlin.union.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class OptionalTest {

    @Test
    fun testNone() {
        val none = Optional.None
        assertFails {
            none.getOrThrow()
        }
        assertTrue(!none.isSome)
        assertEquals(none.toNullable(), null)
    }

    @Test
    fun testSome() {
        val some = Optional.Some(42)
        assertEquals(some.getOrThrow(), 42)
        assertTrue(some.isSome)
        assertEquals(some.toNullable(), 42)
    }

    @Test
    fun orElse() {
        val none = Optional.None
        val some = Optional.Some(42)
        assertEquals(none.orElse{ -1 }, -1)
        assertEquals(some.orElse{ -1 }, 42)
    }

    @Test
    fun asSomeAndAsNone() {
        val value = 42
        val none = value.asNone()
        assertEquals(none, Optional.None)
        val some = value.asSome()
        assertEquals(some, Optional.Some(value))
    }

    @Test
    fun runSome() {
        val some = Optional.Some(42)
        val resultSome = some.runSome {
            this * 2
        }
        assertEquals(resultSome, Optional.Some(84))

        val none: Optional<Int> = Optional.None
        val resultNone = none.runSome {
            this * 2
        }
        assertEquals(resultNone, Optional.None)
    }

    @Test
    fun applySome() {
        val some = Optional.Some(42)
        var valueSome = 1
        val resultSome = some.applySome {
            valueSome = this * 2
        }
        assertEquals(resultSome, Optional.Some(42))
        assertEquals(valueSome, 84)

        val none: Optional<Int> = Optional.None
        var valueNone = 1
        val resultNone = none.applySome {
            valueNone = this * 2
        }
        assertEquals(resultNone, Optional.None)
        assertEquals(valueNone, 1)
    }

    @Test
    fun letSome() {
        val some = Optional.Some(42)
        val resultSome = some.letSome {
            it * 2
        }
        assertEquals(resultSome, Optional.Some(84))

        val none: Optional<Int> = Optional.None
        val resultNone = none.letSome {
            it * 2
        }
        assertEquals(resultNone, Optional.None)
    }

    @Test
    fun alsoSome() {
        val some: Optional<Int> = Optional.Some(42)
        var valueSome = 1
        some.alsoSome {
            valueSome = it * 2
        }
        assertEquals(valueSome, 84)

        val none: Optional<Int> = Optional.None
        var valueNone = 1
        none.alsoSome {
            valueNone = it * 2
        }
        assertEquals(valueNone, 1)
    }

    @Test
    fun takeIfSome() {
        val some: Optional<Int> = Optional.Some(42)
        val resultSome1 = some.takeIfSome {
            this != 42
        }
        assertEquals(resultSome1, Optional.None)
        val resultSome2 = some.takeIfSome {
            this == 42
        }
        assertEquals(resultSome2, Optional.Some(42))

        val none: Optional<Int> = Optional.None
        val resultNone1 = none.takeIfSome {
            this != 42
        }
        assertEquals(resultNone1, Optional.None)
        val resultNone2 = none.takeIfSome {
            this == 42
        }
        assertEquals(resultNone2, Optional.None)
    }

    @Test
    fun takeUnlessSome() {
        val some: Optional<Int> = Optional.Some(42)
        val resultSome1 = some.takeUnlessSome {
            this != 42
        }
        assertEquals(resultSome1, Optional.Some(42))
        val resultSome2 = some.takeUnlessSome {
            this == 42
        }
        assertEquals(resultSome2, Optional.None)

        val none: Optional<Int> = Optional.None
        val resultNone1 = none.takeUnlessSome {
            this != 42
        }
        assertEquals(resultNone1, Optional.None)
        val resultNone2 = none.takeUnlessSome {
            this == 42
        }
        assertEquals(resultNone2, Optional.None)
    }

    @Test
    fun flatten() {
        val someSome = Optional.Some(Optional.Some(42))
        val resultSomeSome = someSome.flatten()
        assertEquals(resultSomeSome, Optional.Some(42))

        val someNone = Optional.Some(Optional.None)
        val resultSomeNone = someNone.flatten()
        assertEquals(resultSomeNone, Optional.None)

        val none: Optional<Optional<Int>> = Optional.None
        val resultNone = none.flatten()
        assertEquals(resultNone, Optional.None)
    }
}