package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodUnionTest {
    @Test
    fun `parse should succeed with value matching first schema`() {
        val unionSchema = Zod.union(Zod.string(), Zod.number())
        assertEquals("hello", unionSchema.parse("hello"))
    }

    @Test
    fun `parse should succeed with value matching second schema`() {
        val unionSchema = Zod.union(Zod.string(), Zod.number())
        assertEquals(42.0, unionSchema.parse(42))
    }

    @Test
    fun `parse should throw with value matching no schema`() {
        val unionSchema = Zod.union(Zod.string(), Zod.number())
        assertFailsWith<IllegalArgumentException> {
            unionSchema.parse(true)
        }
    }

    @Test
    fun `safeParse should return Success with value matching any schema`() {
        val unionSchema = Zod.union(Zod.string(), Zod.number())
        
        val result1 = unionSchema.safeParse("hello")
        assertTrue(result1 is ZodResult.Success)
        assertEquals("hello", result1.data)
        
        val result2 = unionSchema.safeParse(42)
        assertTrue(result2 is ZodResult.Success)
        assertEquals(42.0, result2.data)
    }

    @Test
    fun `safeParse should return Failure with value matching no schema`() {
        val unionSchema = Zod.union(Zod.string(), Zod.number())
        val result = unionSchema.safeParse(true)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Union validation failed"))
    }

    @Test
    fun `union with more than two schemas should work`() {
        val unionSchema = Zod.union(Zod.string(), Zod.number(), Zod.boolean())
        
        assertEquals("hello", unionSchema.parse("hello"))
        assertEquals(42.0, unionSchema.parse(42))
        assertEquals(true, unionSchema.parse(true))
        
        val result = unionSchema.safeParse(listOf(1, 2, 3))
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Union validation failed"))
    }

    @Test
    fun `union with collection should work`() {
        val schemas = listOf(Zod.string(), Zod.number())
        val unionSchema = Zod.union(schemas)
        
        assertEquals("hello", unionSchema.parse("hello"))
        assertEquals(42.0, unionSchema.parse(42))
        
        val result = unionSchema.safeParse(true)
        assertTrue(result is ZodResult.Failure)
    }
}