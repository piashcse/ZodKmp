package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodIntersectionTest {
    @Test
    fun `parse should succeed when value satisfies both schemas`() {
        // Create two schemas that accept numbers but with different constraints
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        // Should pass - positive and less than 100
        val result = intersectionSchema.parse(50)
        assertEquals(50.0, (result as Pair<*, *>).first)
        assertEquals(50.0, result.second)
    }

    @Test
    fun `parse should throw when value fails first schema`() {
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        assertFailsWith<IllegalArgumentException> {
            intersectionSchema.parse(-5)  // Fails positive check
        }
    }

    @Test
    fun `parse should throw when value fails second schema`() {
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        assertFailsWith<IllegalArgumentException> {
            intersectionSchema.parse(150)  // Fails max check
        }
    }

    @Test
    fun `parse should throw when value fails both schemas`() {
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        assertFailsWith<IllegalArgumentException> {
            intersectionSchema.parse(-150)  // Fails both checks
        }
    }

    @Test
    fun `safeParse should return Success when value satisfies both schemas`() {
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        val result = intersectionSchema.safeParse(50)
        assertTrue(result is ZodResult.Success)
        val pairResult = result.data
        assertEquals(50.0, pairResult.first)
        assertEquals(50.0, pairResult.second)
    }

    @Test
    fun `safeParse should return Failure when value fails first schema`() {
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        val result = intersectionSchema.safeParse(-5)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Left schema validation failed"))
    }

    @Test
    fun `safeParse should return Failure when value fails second schema`() {
        val positiveSchema = Zod.number().positive()
        val lessThan100Schema = Zod.number().max(100.0)
        val intersectionSchema = Zod.intersection(positiveSchema, lessThan100Schema)
        
        val result = intersectionSchema.safeParse(150)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Right schema validation failed"))
    }

    @Test
    fun `intersection with different types should work if the input is suitable for both`() {
        // This is a more complex case. For this library's implementation of intersection,
        // typically both schemas would need to accept the same input type.
        // Let's test with schemas that may accept the same input differently.
        val stringSchema = Zod.string().min(3)
        val literalSchema = Zod.literal("hello")
        val intersectionSchema = Zod.intersection(stringSchema, literalSchema)
        
        // "hello" should work for both schemas
        val result = intersectionSchema.safeParse("hello")
        assertTrue(result is ZodResult.Success)
        val pairResult = result.data
        assertEquals("hello", pairResult.first)
        assertEquals("hello", pairResult.second)
        
        // "hi" fails the min(3) constraint
        val result2 = intersectionSchema.safeParse("hi")
        assertTrue(result2 is ZodResult.Failure)
        
        // "world" fails the literal constraint
        val result3 = intersectionSchema.safeParse("world")
        assertTrue(result3 is ZodResult.Failure)
    }
}