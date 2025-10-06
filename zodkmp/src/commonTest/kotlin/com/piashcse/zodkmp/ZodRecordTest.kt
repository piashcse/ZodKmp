package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodRecordTest {
    @Test
    fun `parse should succeed with valid record`() {
        val recordSchema = Zod.record(Zod.string())
        val result = recordSchema.parse(mapOf("name" to "John", "city" to "NYC"))
        assertEquals(mapOf("name" to "John", "city" to "NYC"), result)
    }

    @Test
    fun `parse should work with mixed value types that match the schema`() {
        val recordSchema = Zod.record(Zod.number())
        val result = recordSchema.parse(mapOf("a" to 1, "b" to 2.5))
        assertEquals(mapOf("a" to 1.0, "b" to 2.5), result)
    }

    @Test
    fun `parse should throw with non-record input`() {
        val recordSchema = Zod.record(Zod.string())
        
        assertFailsWith<IllegalArgumentException> {
            recordSchema.parse("not a record")
        }
        
        assertFailsWith<IllegalArgumentException> {
            recordSchema.parse(123)
        }
    }

    @Test
    fun `parse should throw with record containing invalid values`() {
        val recordSchema = Zod.record(Zod.string())
        
        assertFailsWith<IllegalArgumentException> {
            recordSchema.parse(mapOf("name" to "John", "age" to 30))  // age should be string
        }
    }

    @Test
    fun `safeParse should return Success with valid record`() {
        val recordSchema = Zod.record(Zod.string())
        val result = recordSchema.safeParse(mapOf("name" to "John", "city" to "NYC"))
        assertTrue(result is ZodResult.Success)
        assertEquals(mapOf("name" to "John", "city" to "NYC"), result.data)
    }

    @Test
    fun `safeParse should return Failure with non-record input`() {
        val recordSchema = Zod.record(Zod.string())
        val result = recordSchema.safeParse("not a record")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected record"))
    }

    @Test
    fun `safeParse should return Failure with record containing invalid values`() {
        val recordSchema = Zod.record(Zod.string())
        val result = recordSchema.safeParse(mapOf("name" to "John", "age" to 30))
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("age") })  // Error should reference the 'age' key
        assertTrue(result.error.errors.any { it.contains("Expected string") })
    }

    @Test
    fun `empty record should pass`() {
        val recordSchema = Zod.record(Zod.string())
        val result = recordSchema.safeParse(mapOf<String, Any>())
        assertTrue(result is ZodResult.Success)
        assertTrue(result.data.isEmpty())
    }
}