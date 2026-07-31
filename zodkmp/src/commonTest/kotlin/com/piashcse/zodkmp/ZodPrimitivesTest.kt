package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodPrimitivesTest {

    @Test
    fun `any should accept any value`() {
        assertTrue(Zod.any().safeParse("hello") is ZodResult.Success)
        assertTrue(Zod.any().safeParse(42) is ZodResult.Success)
        assertTrue(Zod.any().safeParse(null) is ZodResult.Success)
        assertEquals("hello", Zod.any().parse("hello"))
    }

    @Test
    fun `unknown should accept any value`() {
        assertTrue(Zod.unknown().safeParse(3.14) is ZodResult.Success)
        assertTrue(Zod.unknown().safeParse(null) is ZodResult.Success)
    }

    @Test
    fun `never should reject every value`() {
        assertTrue(Zod.never().safeParse("anything") is ZodResult.Failure)
        assertTrue(Zod.never().safeParse(null) is ZodResult.Failure)
    }

    @Test
    fun `void should accept null and return unit`() {
        val result = Zod.void().safeParse(null)
        assertTrue(result is ZodResult.Success)
        assertEquals(Unit, result.data)
    }

    @Test
    fun `void should reject non null`() {
        assertTrue(Zod.void().safeParse(42) is ZodResult.Failure)
    }

    @Test
    fun `long should accept longs`() {
        val result = Zod.long().safeParse(9223372036854775807L)
        assertTrue(result is ZodResult.Success)
        assertEquals(9223372036854775807L, result.data)
    }

    @Test
    fun `long should accept integral numbers and numeric strings`() {
        assertEquals(42L, (Zod.long().safeParse(42) as ZodResult.Success).data)
        assertEquals(7L, (Zod.long().safeParse("7") as ZodResult.Success).data)
    }

    @Test
    fun `long should reject non integral values`() {
        assertTrue(Zod.long().safeParse(3.14) is ZodResult.Failure)
        assertTrue(Zod.long().safeParse("not-a-long") is ZodResult.Failure)
    }

    @Test
    fun `long validations should work`() {
        val schema = Zod.long().positive().max(100)
        assertTrue(schema.safeParse(50L) is ZodResult.Success)
        assertTrue(schema.safeParse(-1L) is ZodResult.Failure)
        assertTrue(schema.safeParse(200L) is ZodResult.Failure)
    }

    @Test
    fun `set should validate elements`() {
        val schema = Zod.set(Zod.number().positive())
        val result = schema.safeParse(setOf(1, 2, 3))
        assertTrue(result is ZodResult.Success)
        assertEquals(setOf(1.0, 2.0, 3.0), result.data)
    }

    @Test
    fun `set should reject invalid elements`() {
        val schema = Zod.set(Zod.number().positive())
        assertTrue(schema.safeParse(setOf(1, -2, 3)) is ZodResult.Failure)
    }

    @Test
    fun `set should accept lists too`() {
        val schema = Zod.set(Zod.string())
        val result = schema.safeParse(listOf("a", "b"))
        assertTrue(result is ZodResult.Success)
        assertEquals(setOf("a", "b"), result.data)
    }

    @Test
    fun `set size validations should work`() {
        val schema = Zod.set(Zod.number()).min(2)
        assertTrue(schema.safeParse(setOf(1, 2)) is ZodResult.Success)
        assertTrue(schema.safeParse(setOf(1)) is ZodResult.Failure)
    }

    @Test
    fun `map should validate keys and values`() {
        val schema = Zod.map(Zod.string(), Zod.number())
        val result = schema.safeParse(mapOf("a" to 1, "b" to 2))
        assertTrue(result is ZodResult.Success)
        assertEquals(mapOf("a" to 1.0, "b" to 2.0), result.data)
    }

    @Test
    fun `map should reject invalid values`() {
        val schema = Zod.map(Zod.string(), Zod.number())
        assertTrue(schema.safeParse(mapOf("a" to 1, "b" to "not-number")) is ZodResult.Failure)
    }

    @Test
    fun `map should reject invalid keys`() {
        val schema = Zod.map(Zod.string().min(2), Zod.number())
        assertTrue(schema.safeParse(mapOf("a" to 1)) is ZodResult.Failure)
    }

    @Test
    fun `instanceOf should accept instances`() {
        val schema = Zod.instanceOf<String>()
        assertTrue(schema.safeParse("hello") is ZodResult.Success)
        assertTrue(schema.safeParse(42) is ZodResult.Failure)
    }

    @Test
    fun `instanceOf should work with custom classes`() {
        class Person(val name: String)
        val schema = Zod.instanceOf<Person>()
        assertTrue(schema.safeParse(Person("bob")) is ZodResult.Success)
        assertTrue(schema.safeParse("bob") is ZodResult.Failure)
    }
}
