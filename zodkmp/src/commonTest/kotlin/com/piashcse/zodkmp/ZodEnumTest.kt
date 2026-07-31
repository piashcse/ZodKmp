package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

enum class Role { ADMIN, USER, GUEST }

class ZodEnumTest {
    @Test
    fun `parse should succeed with valid enum value`() {
        val stringEnumSchema = Zod.enum("red", "green", "blue")
        assertEquals("red", stringEnumSchema.parse("red"))
        assertEquals("green", stringEnumSchema.parse("green"))
        assertEquals("blue", stringEnumSchema.parse("blue"))
        
        val numberEnumSchema = Zod.enum(1, 2, 3)
        assertEquals(1, numberEnumSchema.parse(1))
        assertEquals(2, numberEnumSchema.parse(2))
        assertEquals(3, numberEnumSchema.parse(3))
    }

    @Test
    fun `parse should throw with invalid enum value`() {
        val stringEnumSchema = Zod.enum("red", "green", "blue")
        assertFailsWith<IllegalArgumentException> {
            stringEnumSchema.parse("yellow")
        }
    }

    @Test
    fun `safeParse should return Success with valid enum value`() {
        val stringEnumSchema = Zod.enum("red", "green", "blue")
        val result = stringEnumSchema.safeParse("red")
        assertTrue(result is ZodResult.Success)
        assertEquals("red", result.data)
    }

    @Test
    fun `safeParse should return Failure with invalid enum value`() {
        val stringEnumSchema = Zod.enum("red", "green", "blue")
        val result = stringEnumSchema.safeParse("yellow")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Valid values: red, green, blue"))
    }

    @Test
    fun `enum with collection should work`() {
        val colors = listOf("red", "green", "blue")
        val stringEnumSchema = Zod.enum(colors)
        
        assertEquals("red", stringEnumSchema.parse("red"))
        
        val result = stringEnumSchema.safeParse("yellow")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Valid values: red, green, blue"))
    }

    @Test
    fun `enum from kotlin enum class should accept enum values`() {
        val schema = Zod.enum(Role::class)
        assertEquals(Role.ADMIN, schema.parse(Role.ADMIN))
        assertEquals(Role.USER, schema.parse(Role.USER))
    }

    @Test
    fun `enum from kotlin enum class should accept enum names`() {
        val schema = Zod.enum(Role::class)
        assertEquals(Role.GUEST, schema.parse("GUEST"))
        assertEquals(Role.ADMIN, (schema.safeParse("ADMIN") as ZodResult.Success).data)
    }

    @Test
    fun `enum from kotlin enum class should reject invalid names`() {
        val schema = Zod.enum(Role::class)
        assertTrue(schema.safeParse("SUPERUSER") is ZodResult.Failure)
        assertTrue(schema.safeParse("admin") is ZodResult.Failure)
    }
}