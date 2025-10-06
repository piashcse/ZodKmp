package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodRefinementTest {
    @Test
    fun `refine should work with basic schema`() {
        val positiveNumberSchema = Zod.number().refine({ it > 0 }) { "Number must be positive" }
        
        // Should pass
        assertEquals(5.0, positiveNumberSchema.parse(5))
        
        // Should fail refinement
        val result = positiveNumberSchema.safeParse(-5)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Number must be positive") })
    }

    @Test
    fun `refine should still validate base schema first`() {
        val positiveNumberSchema = Zod.number().refine({ it > 0 }) { "Number must be positive" }
        
        // Should fail base schema validation first
        val result = positiveNumberSchema.safeParse("not a number")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Expected number") })
    }

    @Test
    fun `multiple refinements should work`() {
        val schema = Zod.number().refine({ it > 0 }) { "Number must be positive" }
            .refine({ it < 100 }) { "Number must be less than 100" }
        
        // Should pass both refinements
        assertEquals(50.0, schema.parse(50))
        
        // Should fail first refinement
        val result1 = schema.safeParse(-5)
        assertTrue(result1 is ZodResult.Failure)
        
        // Should fail second refinement
        val result2 = schema.safeParse(150)
        assertTrue(result2 is ZodResult.Failure)
    }

    @Test
    fun `refine should work with string schema`() {
        val emailSchema = Zod.string().refine({ 
            it.contains("@") && it.contains(".") 
        }) { "Invalid email format: $it" }
        
        // Should pass
        assertEquals("test@example.com", emailSchema.parse("test@example.com"))
        
        // Should fail refinement
        val result = emailSchema.safeParse("invalid-email")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Invalid email format") })
    }

    @Test
    fun `refine with default value should work`() {
        val positiveNumberSchema = Zod.number().refine({ it > 0 }) { "Number must be positive" }
            .default { 10.0 }
        
        // With null should return default
        assertEquals(10.0, positiveNumberSchema.parse(null))
        
        // With valid value should return parsed value
        assertEquals(20.0, positiveNumberSchema.parse(20))
        
        // With invalid value should fail refinement
        assertFailsWith<IllegalArgumentException> {
            positiveNumberSchema.parse(-5)
        }
    }

    @Test
    fun `refine with nullable should work`() {
        val positiveNumberSchema = Zod.number().refine({ it > 0 }) { "Number must be positive" }
            .nullable()
        
        // With null should return null
        assertEquals(null, positiveNumberSchema.parse(null))
        
        // With valid value should return parsed value
        assertEquals(20.0, positiveNumberSchema.parse(20))
        
        // With invalid value should fail refinement
        val result = positiveNumberSchema.safeParse(-5)
        assertTrue(result is ZodResult.Failure)
    }
}