package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodWrappersTest {

    @Test
    fun `preprocess should transform input before validation`() {
        val schema = Zod.preprocess({ (it as? String)?.trim() }, Zod.string().min(3))
        val result = schema.safeParse("  hello  ")
        assertTrue(result is ZodResult.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun `preprocess extension should work`() {
        val schema = Zod.string().preprocess { (it as? String)?.trim() }
        assertTrue(schema.safeParse("  abc  ") is ZodResult.Success)
    }

    @Test
    fun `preprocess should fail if processed input is invalid`() {
        val schema = Zod.preprocess({ it.toString() }, Zod.number())
        assertTrue(schema.safeParse(42) is ZodResult.Success)
        assertTrue(schema.safeParse("not-number") is ZodResult.Failure)
    }

    @Test
    fun `catch should return fallback value on failure`() {
        val schema = Zod.string().catch("default")
        assertEquals("hello", schema.parse("hello"))
        assertEquals("default", schema.parse(42))
        val result = schema.safeParse(42)
        assertTrue(result is ZodResult.Success)
        assertEquals("default", result.data)
    }

    @Test
    fun `catch should support function fallback`() {
        val schema = Zod.number().catch { input -> (input as? String)?.toDoubleOrNull() ?: -1.0 }
        val result = schema.safeParse("not-number")
        assertTrue(result is ZodResult.Success)
        assertEquals(-1.0, result.data)
    }

    @Test
    fun `pipe should feed first output into second schema`() {
        val trimThenLength = Zod.string().trim().pipe(Zod.number())
        // First schema trims to a string; the second coerces the string to a number.
        val result = trimThenLength.safeParse("  42  ")
        assertTrue(result is ZodResult.Success)
        assertEquals(42.0, result.data)
    }

    @Test
    fun `pipe should fail on first schema failure`() {
        val schema = Zod.string().pipe(Zod.number())
        assertTrue(schema.safeParse(42) is ZodResult.Failure)
    }

    @Test
    fun `pipe factory should work`() {
        val schema = Zod.pipe(Zod.number().transform { (it * 2) }, Zod.number().positive())
        val result = schema.safeParse(10)
        assertTrue(result is ZodResult.Success)
        assertEquals(20.0, result.data)
    }

    @Test
    fun `describe should not change validation behaviour`() {
        val schema = Zod.string().min(2).describe("A name")
        assertTrue(schema is ZodDescribe<*>)
        assertEquals("A name", schema.description)
        assertTrue(schema.safeParse("ab") is ZodResult.Success)
        assertTrue(schema.safeParse("a") is ZodResult.Failure)
    }

    @Test
    fun `meta should attach metadata`() {
        val schema = Zod.number().meta(mapOf("example" to 42))
        assertTrue(schema is ZodDescribe<*>)
        assertEquals(42, schema.metadata["example"])
        assertTrue(schema.safeParse(1) is ZodResult.Success)
    }
}
