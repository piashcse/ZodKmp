package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodDefaultTest {
    @Test
    fun `parse should return default value when input is null`() {
        val schema = Zod.string().default { "default_value" }
        val result = schema.parse(null)
        assertEquals("default_value", result)
    }

    @Test
    fun `parse should return parsed value when input is not null`() {
        val schema = Zod.string().default { "default_value" }
        val result = schema.parse("actual_value")
        assertEquals("actual_value", result)
    }

    @Test
    fun `safeParse should return default value when input is null`() {
        val schema = Zod.string().default { "default_value" }
        val result = schema.safeParse(null)
        assertTrue(result is ZodResult.Success<*>)
        assertEquals("default_value", result.data)
    }

    @Test
    fun `safeParse should return parsed value when input is not null`() {
        val schema = Zod.string().default { "default_value" }
        val result = schema.safeParse("actual_value")
        assertTrue(result is ZodResult.Success<*>)
        assertEquals("actual_value", result.data)
    }

    @Test
    fun `default with lambda should work`() {
        val schema = Zod.number().default { 42.0 }
        
        // With null input, should return lambda result
        assertEquals(42.0, schema.parse(null))
        
        // With actual input, should parse the input
        assertEquals(10.0, schema.parse(10))
    }

    @Test
    fun `default value should work with complex schemas`() {
        val stringWithMin = Zod.string().min(3)
        val schema = stringWithMin.default { "hello" }  // Default meets min length requirement
        
        // With null input, should return default
        assertEquals("hello", schema.parse(null))
        
        // With valid input, should parse normally
        assertEquals("world", schema.parse("world"))
        
        // With invalid input, should still validate
        val result = schema.safeParse("hi")  // Too short
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `default should work with various types`() {
        // String
        val stringSchema = Zod.string().default { "default" }
        assertEquals("default", stringSchema.parse(null))
        
        // Number
        val numberSchema = Zod.number().default { 100.0 }
        assertEquals(100.0, numberSchema.parse(null))
        
        // Boolean
        val boolSchema = Zod.boolean().default { false }
        assertEquals(false, boolSchema.parse(null))
    }
}