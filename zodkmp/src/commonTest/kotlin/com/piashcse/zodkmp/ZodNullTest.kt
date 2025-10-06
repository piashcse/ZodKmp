package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodNullTest {
    private val basicNullSchema = Zod.nullType()

    @Test
    fun `parse should succeed with null input`() {
        val result = basicNullSchema.parse(null)
        assertEquals(null, result)
    }

    @Test
    fun `parse should throw with non-null input`() {
        assertFailsWith<IllegalArgumentException> {
            basicNullSchema.parse("not null")
        }
        
        assertFailsWith<IllegalArgumentException> {
            basicNullSchema.parse(123)
        }
        
        assertFailsWith<IllegalArgumentException> {
            basicNullSchema.parse(true)
        }
    }

    @Test
    fun `safeParse should return Success with null input`() {
        val result = basicNullSchema.safeParse(null)
        assertTrue(result is ZodResult.Success)
        assertEquals(null, result.data)
    }

    @Test
    fun `safeParse should return Failure with non-null input`() {
        val result = basicNullSchema.safeParse("not null")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected null"))
    }
}