package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodLiteralTest {
    @Test
    fun `parse should succeed with matching literal value`() {
        val stringLiteralSchema = Zod.literal("hello")
        assertEquals("hello", stringLiteralSchema.parse("hello"))
        
        val numberLiteralSchema = Zod.literal(42)
        assertEquals(42, numberLiteralSchema.parse(42))
        
        val booleanLiteralSchema = Zod.literal(true)
        assertEquals(true, booleanLiteralSchema.parse(true))
    }

    @Test
    fun `parse should throw with non-matching literal value`() {
        val stringLiteralSchema = Zod.literal("hello")
        assertFailsWith<IllegalArgumentException> {
            stringLiteralSchema.parse("world")
        }
        
        val numberLiteralSchema = Zod.literal(42)
        assertFailsWith<IllegalArgumentException> {
            numberLiteralSchema.parse(43)
        }
    }

    @Test
    fun `safeParse should return Success with matching literal value`() {
        val stringLiteralSchema = Zod.literal("hello")
        val result = stringLiteralSchema.safeParse("hello")
        assertTrue(result is ZodResult.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun `safeParse should return Failure with non-matching literal value`() {
        val stringLiteralSchema = Zod.literal("hello")
        val result = stringLiteralSchema.safeParse("world")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected literal hello"))
    }

    @Test
    fun `safeParse should return Failure with wrong type`() {
        val stringLiteralSchema = Zod.literal("hello")
        val result = stringLiteralSchema.safeParse(123)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected literal hello"))
    }
}