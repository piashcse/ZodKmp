package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodArrayTest {
    private val basicStringArraySchema: ZodArray<String> = ZodArray.schema(Zod.string())
    private val basicNumberArraySchema: ZodArray<Double> = ZodArray.schema(Zod.number())

    @Test
    fun `parse should succeed with valid array`() {
        val result = basicStringArraySchema.parse(listOf("a", "b", "c"))
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `parse should work with arrays`() {
        val result = basicStringArraySchema.parse(arrayOf("a", "b", "c"))
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `parse should throw with non-array input`() {
        assertFailsWith<IllegalArgumentException> {
            basicStringArraySchema.parse("not an array")
        }
        
        assertFailsWith<IllegalArgumentException> {
            basicStringArraySchema.parse(123)
        }
    }

    @Test
    fun `parse should throw with array containing invalid elements`() {
        assertFailsWith<IllegalArgumentException> {
            basicStringArraySchema.parse(listOf("a", 123, "c"))
        }
    }

    @Test
    fun `safeParse should return Success with valid array`() {
        val result = basicStringArraySchema.safeParse(listOf("a", "b", "c"))
        assertTrue(result is ZodResult.Success)
        assertEquals(listOf("a", "b", "c"), result.data)
    }

    @Test
    fun `safeParse should return Failure with non-array input`() {
        val result = basicStringArraySchema.safeParse("not an array")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected array"))
    }

    @Test
    fun `safeParse should return Failure with array containing invalid elements`() {
        val result = basicStringArraySchema.safeParse(listOf("a", 123, "c"))
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("[1]") }) // Error should be at index 1
    }

    @Test
    fun `min validation should work`() {
        val schema: ZodArray<Double> = basicNumberArraySchema.min(2)
        
        // Should pass
        assertEquals(listOf(1.0, 2.0, 3.0), schema.parse(listOf(1, 2, 3)))
        
        // Should fail
        val result = schema.safeParse(listOf(1))
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("at least 2 element") })
    }

    @Test
    fun `max validation should work`() {
        val schema: ZodArray<Double> = basicNumberArraySchema.max(2)
        
        // Should pass
        assertEquals(listOf(1.0, 2.0), schema.parse(listOf(1, 2)))
        
        // Should fail
        val result = schema.safeParse(listOf(1, 2, 3))
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("at most 2 element") })
    }

    @Test
    fun `length validation should work`() {
        val schema: ZodArray<Double> = basicNumberArraySchema.length(3)
        
        // Should pass
        assertEquals(listOf(1.0, 2.0, 3.0), schema.parse(listOf(1, 2, 3)))
        
        // Should fail - too short
        val result1 = schema.safeParse(listOf(1, 2))
        assertTrue(result1 is ZodResult.Failure)
        assertTrue(result1.error.errors.any { it.contains("exactly 3 element") })
        
        // Should fail - too long
        val result2 = schema.safeParse(listOf(1, 2, 3, 4))
        assertTrue(result2 is ZodResult.Failure)
        assertTrue(result2.error.errors.any { it.contains("exactly 3 element") })
    }

    @Test
    fun `nested validation should work`() {
        val schema: ZodArray<String> = ZodArray.schema(Zod.string().min(2)) // Array of strings, each at least 2 chars
        
        // Should pass
        val result1 = schema.safeParse(listOf("hello", "world"))
        assertTrue(result1 is ZodResult.Success<*>)
        assertEquals(listOf("hello", "world"), result1.data)
        
        // Should fail - one element too short
        val result2 = schema.safeParse(listOf("hi", "a"))
        assertTrue(result2 is ZodResult.Failure)
        assertTrue(result2.error.errors.any { it.contains("[1]") }) // Error at index 1
        assertTrue(result2.error.errors.any { it.contains("at least 2 characters") })
    }
}