package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodNullableTest {
    @Test
    fun `parse should return null when input is null`() {
        val schema = Zod.string().nullable()
        val result = schema.parse(null)
        assertEquals(null, result)
    }

    @Test
    fun `parse should return parsed value when input is not null`() {
        val schema = Zod.string().nullable()
        val result = schema.parse("hello")
        assertEquals("hello", result)
    }

    @Test
    fun `safeParse should return null when input is null`() {
        val schema = Zod.string().nullable()
        val result = schema.safeParse(null)
        assertTrue(result is ZodResult.Success)
        assertEquals(null, result.data)
    }

    @Test
    fun `safeParse should return parsed value when input is not null and valid`() {
        val schema = Zod.string().nullable()
        val result = schema.safeParse("hello")
        assertTrue(result is ZodResult.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun `safeParse should return Failure when input is not null but invalid`() {
        val schema = Zod.string().nullable()
        val result = schema.safeParse(123)  // Not a string
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `nullable should work with complex schemas`() {
        val stringWithMin = Zod.string().min(3)
        val nullableSchema = stringWithMin.nullable()
        
        // With null input, should return null
        assertEquals(null, nullableSchema.parse(null))
        
        // With valid input, should parse normally
        assertEquals("hello", nullableSchema.parse("hello"))
        
        // With invalid input, should fail validation
        val result = nullableSchema.safeParse("hi")  // Too short
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `nullable should work with various types`() {
        // String
        val stringSchema = Zod.string().nullable()
        assertEquals(null, stringSchema.parse(null))
        assertEquals("test", stringSchema.parse("test"))
        
        // Number
        val numberSchema = Zod.number().nullable()
        assertEquals(null, numberSchema.parse(null))
        assertEquals(42.0, numberSchema.parse(42))
        
        // Boolean
        val boolSchema = Zod.boolean().nullable()
        assertEquals(null, boolSchema.parse(null))
        assertEquals(true, boolSchema.parse(true))
    }
}