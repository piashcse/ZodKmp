package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodTupleTest {
    @Test
    fun `parse should succeed with valid tuple`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number(), Zod.boolean())
        val result = tupleSchema.parse(listOf<Any>("hello", 42, true))
        assertEquals(listOf("hello", 42.0, true), result)
    }

    @Test
    fun `parse should work with arrays`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number())
        val result = tupleSchema.parse(arrayOf<Any>("hello", 42))
        assertEquals(listOf("hello", 42.0), result)
    }

    @Test
    fun `parse should throw with wrong length`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number())
        
        assertFailsWith<IllegalArgumentException> {
            tupleSchema.parse(listOf("hello"))  // Too short
        }
        
        assertFailsWith<IllegalArgumentException> {
            tupleSchema.parse(listOf<Any>("hello", 42, "extra"))  // Too long
        }
    }

    @Test
    fun `parse should throw with invalid elements`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number())
        
        assertFailsWith<IllegalArgumentException> {
            tupleSchema.parse(listOf<Any>(42, "hello"))  // Wrong types
        }
    }

    @Test
    fun `safeParse should return Success with valid tuple`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number(), Zod.boolean())
        val result = tupleSchema.safeParse(listOf<Any>("hello", 42, true))
        assertTrue(result is ZodResult.Success)
        assertEquals(listOf("hello", 42.0, true), result.data)
    }

    @Test
    fun `safeParse should return Failure with wrong length`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number())
        val result = tupleSchema.safeParse(listOf("hello"))  // Too short
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("exactly 2 element"))
    }

    @Test
    fun `safeParse should return Failure with invalid elements`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number())
        val result = tupleSchema.safeParse(listOf<Any>(42, "hello"))  // Wrong types
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("[0]") })  // Error at position 0
        assertTrue(result.error.errors.any { it.contains("[1]") })  // Error at position 1
    }

    @Test
    fun `safeParse should return Failure with non-array input`() {
        val tupleSchema = Zod.tuple(Zod.string(), Zod.number())
        val result = tupleSchema.safeParse("not a tuple")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected tuple"))
    }

    @Test
    fun `tuple with collection should work`() {
        val schemas = listOf(Zod.string(), Zod.number())
        val tupleSchema = Zod.tuple(schemas)
        
        val result = tupleSchema.safeParse(listOf<Any>("hello", 42))
        assertTrue(result is ZodResult.Success)
        assertEquals(listOf("hello", 42.0), result.data)
    }
}