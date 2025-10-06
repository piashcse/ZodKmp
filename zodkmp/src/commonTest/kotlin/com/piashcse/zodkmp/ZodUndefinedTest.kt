package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodUndefinedTest {
    private val basicUndefinedSchema = Zod.undefined()

    @Test
    fun `parse should succeed with null input representing undefined`() {
        val result = basicUndefinedSchema.parse(null)
        assertEquals(null, result)
    }

    @Test
    fun `parse should throw with non-null input`() {
        assertFailsWith<IllegalArgumentException> {
            basicUndefinedSchema.parse("not undefined")
        }
        
        assertFailsWith<IllegalArgumentException> {
            basicUndefinedSchema.parse(123)
        }
        
        assertFailsWith<IllegalArgumentException> {
            basicUndefinedSchema.parse(true)
        }
    }

    @Test
    fun `safeParse should return Success with null input representing undefined`() {
        val result = basicUndefinedSchema.safeParse(null)
        assertTrue(result is ZodResult.Success)
        assertEquals(null, result.data)
    }

    @Test
    fun `safeParse should return Failure with non-null input`() {
        val result = basicUndefinedSchema.safeParse("not undefined")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected undefined"))
    }
}