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

    @Test
    fun `record with key schema should validate keys and values`() {
        val schema = Zod.record(Zod.string().min(2), Zod.number())
        val result = schema.safeParse(mapOf("aa" to 1, "bb" to 2))
        assertTrue(result is ZodResult.Success)
        assertEquals(mapOf("aa" to 1.0, "bb" to 2.0), result.data)
    }

    @Test
    fun `record with key schema should reject invalid keys`() {
        val schema = Zod.record(Zod.string().min(2), Zod.number())
        val result = schema.safeParse(mapOf("a" to 1))
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `record with key schema should reject invalid values`() {
        val schema = Zod.record(Zod.string(), Zod.number())
        val result = schema.safeParse(mapOf("a" to "not-number"))
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `looseRecord should not reject non matching keys`() {
        val schema = Zod.looseRecord(Zod.string().min(3), Zod.number())
        // A non-matching key with a valid value passes.
        assertTrue(schema.safeParse(mapOf("aaa" to 1, "x" to 2)) is ZodResult.Success)
    }

    @Test
    fun `looseRecord should still validate values of non matching keys`() {
        val schema = Zod.looseRecord(Zod.string().min(3), Zod.number())
        assertTrue(schema.safeParse(mapOf("aaa" to "bad")) is ZodResult.Failure)
        assertTrue(schema.safeParse(mapOf("x" to "bad")) is ZodResult.Failure)
    }

    @Test
    fun `strictRecord should reject non matching keys`() {
        val schema = Zod.strictRecord(Zod.string().min(3), Zod.number())
        val result = schema.safeParse(mapOf("a" to 1))
        assertTrue(result is ZodResult.Failure)
    }
}