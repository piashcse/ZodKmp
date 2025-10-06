package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodTransformTest {
    @Test
    fun `transform should work with basic schema`() {
        val stringToUppercase = Zod.string().transform { it.uppercase() }
        
        val result = stringToUppercase.parse("hello")
        assertEquals("HELLO", result)
    }

    @Test
    fun `transform should still validate base schema first`() {
        val stringToUppercase = Zod.string().transform { it.uppercase() }
        
        assertFailsWith<IllegalArgumentException> {
            stringToUppercase.parse(123)  // Not a string
        }
    }

    @Test
    fun `safeParse should work with transform`() {
        val stringToUppercase = Zod.string().transform { it.uppercase() }
        
        val result = stringToUppercase.safeParse("hello")
        assertTrue(result is ZodResult.Success)
        assertEquals("HELLO", result.data)
    }

    @Test
    fun `safeParse should return Failure for invalid base schema`() {
        val stringToUppercase = Zod.string().transform { it.uppercase() }
        
        val result = stringToUppercase.safeParse(123)
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `safeParse should return Failure for transformation error`() {
        val stringToNumber = Zod.string().transform { 
            if (it == "error") throw RuntimeException("Transformation error") 
            it 
        }
        
        val result = stringToNumber.safeParse("error")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Transformation failed") })
    }

    @Test
    fun `transform should work with number schema`() {
        val numberToDouble = Zod.number().transform { it * 2 }
        
        val result = numberToDouble.parse(5)
        assertEquals(10.0, result)
    }

    @Test
    fun `transform with complex schema should work`() {
        val stringWithMin = Zod.string().min(3)
        val upperWithMin = stringWithMin.transform { it.uppercase() }
        
        // Should pass validation and transform
        assertEquals("HELLO", upperWithMin.parse("hello"))
        
        // Should fail validation before transform
        val result = upperWithMin.safeParse("hi")
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `chained transformations should work`() {
        val finalSchema = Zod.string()
            .transform { it.trim() }
            .transform { it.uppercase() }
            .transform { it + "_SUFFIX" }
        
        val result = finalSchema.parse("  hello  ")
        assertEquals("HELLO_SUFFIX", result)
    }

    @Test
    fun `transform with default should work`() {
        val schema = Zod.string()
            .transform { it.uppercase() }
            .default("DEFAULT")
        
        // With null should return default after transform
        assertEquals("DEFAULT", schema.parse(null))
        
        // With value should transform
        assertEquals("HELLO", schema.parse("hello"))
    }

    @Test
    fun `transform with refine should work`() {
        val schema = Zod.string()
            .refine({ it.length > 3 }) { "String must be longer than 3 chars" }
            .transform { it.uppercase() }
        
        // Should pass validation and transform
        assertEquals("HELLO", schema.parse("hello"))
        
        // Should fail validation before transform
        val result = schema.safeParse("hi")
        assertTrue(result is ZodResult.Failure)
    }
}