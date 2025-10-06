package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodNumberTest {
    private val basicNumberSchema = Zod.number()

    @Test
    fun `parse should succeed with valid number`() {
        val result1 = basicNumberSchema.parse(42)
        assertEquals(42.0, result1)
        val result2 = basicNumberSchema.parse(3.14)
        assertEquals(3.14, result2)
        val result3 = basicNumberSchema.parse("42")
        assertEquals(42.0, result3)
    }

    @Test
    fun `parse should throw with non-number input`() {
        assertFailsWith<IllegalArgumentException> {
            basicNumberSchema.parse("not a number")
        }
    }

    @Test
    fun `safeParse should return Success with valid number`() {
        val result = basicNumberSchema.safeParse(42)
        assertTrue(result is ZodResult.Success)
        assertEquals(42.0, result.data)
    }

    @Test
    fun `safeParse should return Failure with non-number input`() {
        val result = basicNumberSchema.safeParse("not a number")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected number"))
    }

    @Test
    fun `min validation should work`() {
        val schema = basicNumberSchema.min(10.0)
        
        // Should pass
        assertEquals(15.0, schema.parse(15.0))
        
        // Should fail
        val result = schema.safeParse(5.0)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("greater than or equal to 10.0") })
    }

    @Test
    fun `max validation should work`() {
        val schema = basicNumberSchema.max(10.0)
        
        // Should pass
        assertEquals(5.0, schema.parse(5.0))
        
        // Should fail
        val result = schema.safeParse(15.0)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("less than or equal to 10.0") })
    }

    @Test
    fun `int validation should work`() {
        val schema = basicNumberSchema.int()
        
        // Should pass
        assertEquals(5.0, schema.parse(5))
        
        // Should fail
        val result = schema.safeParse(5.5)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must be an integer") })
    }

    @Test
    fun `positive validation should work`() {
        val schema = basicNumberSchema.positive()
        
        // Should pass
        assertEquals(5.0, schema.parse(5))
        
        // Should fail
        val result = schema.safeParse(-5)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must be positive") })
    }

    @Test
    fun `negative validation should work`() {
        val schema = basicNumberSchema.negative()
        
        // Should pass
        assertEquals(-5.0, schema.parse(-5))
        
        // Should fail
        val result = schema.safeParse(5)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must be negative") })
    }

    @Test
    fun `nonNegative validation should work`() {
        val schema = basicNumberSchema.nonNegative()
        
        // Should pass
        assertEquals(0.0, schema.parse(0))  // Zero is non-negative
        assertEquals(5.0, schema.parse(5))
        
        // Should fail
        val result = schema.safeParse(-5)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must be non-negative") })
    }

    @Test
    fun `chained validations should work`() {
        val schema = basicNumberSchema
            .min(0.0)
            .max(100.0)
            .int()
        
        // Should pass
        assertEquals(50.0, schema.parse(50.0))
        
        // Should fail - negative
        val result1 = schema.safeParse(-5.0)
        assertTrue(result1 is ZodResult.Failure)
        assertTrue(result1.error.errors.any { it.contains("greater than or equal to 0.0") })
        
        // Should fail - too large
        val result2 = schema.safeParse(101.0)
        assertTrue(result2 is ZodResult.Failure)
        assertTrue(result2.error.errors.any { it.contains("less than or equal to 100.0") })
        
        // Should fail - not integer
        val result3 = schema.safeParse(50.5)
        assertTrue(result3 is ZodResult.Failure)
        assertTrue(result3.error.errors.any { it.contains("must be an integer") })
    }
}