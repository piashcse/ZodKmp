package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodBooleanTest {
    private val basicBooleanSchema = Zod.boolean()

    @Test
    fun `parse should succeed with valid boolean`() {
        assertEquals(true, basicBooleanSchema.parse(true))
        assertEquals(false, basicBooleanSchema.parse(false))
        assertEquals(true, Zod.coerce.boolean().parse("true"))
        assertEquals(false, Zod.coerce.boolean().parse("false"))
    }

    @Test
    fun `parse should throw with non-boolean input`() {
        assertFailsWith<IllegalArgumentException> {
            basicBooleanSchema.parse("not a boolean")
        }
        
        assertFailsWith<IllegalArgumentException> {
            basicBooleanSchema.parse(123)
        }
    }

    @Test
    fun `safeParse should return Success with valid boolean`() {
        val result = basicBooleanSchema.safeParse(true)
        assertTrue(result is ZodResult.Success)
        assertEquals(true, result.data)
        
        val result2 = basicBooleanSchema.safeParse("false")
        assertTrue(result2 is ZodResult.Failure, "plain boolean schemas must not coerce strings")
    }

    @Test
    fun `safeParse should return Failure with non-boolean input`() {
        val result = basicBooleanSchema.safeParse("not a boolean")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected boolean"))
    }

    @Test
    fun `isEqual validation should work`() {
        val schema = basicBooleanSchema.isEqual(true)
        
        // Should pass
        assertEquals(true, schema.parse(true))
        
        // Should fail
        val result = schema.safeParse(false)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must be equal to true") })
    }

    @Test
    fun `case insensitive string boolean parsing should work with coercion`() {
        val result1 = Zod.coerce.boolean().safeParse("True")
        assertTrue(result1 is ZodResult.Success)
        assertEquals(true, result1.data)
        
        val result2 = Zod.coerce.boolean().safeParse("FALSE")
        assertTrue(result2 is ZodResult.Success)
        assertEquals(false, result2.data)
    }

    @Test
    fun `plain boolean should reject strings without coercion`() {
        assertTrue(basicBooleanSchema.safeParse("true") is ZodResult.Failure)
        assertTrue(Zod.coerce.boolean().safeParse("true") is ZodResult.Success)
    }
}