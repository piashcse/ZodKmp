package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodStringTest {
    private val basicStringSchema = Zod.string()

    @Test
    fun `parse should succeed with valid string`() {
        val result = basicStringSchema.parse("hello")
        assertEquals("hello", result)
    }

    @Test
    fun `parse should throw with non-string input`() {
        assertFailsWith<IllegalArgumentException> {
            basicStringSchema.parse(123)
        }
    }

    @Test
    fun `safeParse should return Success with valid string`() {
        val result = basicStringSchema.safeParse("hello")
        assertTrue(result is ZodResult.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun `safeParse should return Failure with non-string input`() {
        val result = basicStringSchema.safeParse(123)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected string"))
    }

    @Test
    fun `min validation should work`() {
        val schema = basicStringSchema.min(3)
        
        // Should pass
        assertEquals("hello", schema.parse("hello"))
        
        // Should fail
        val result = schema.safeParse("hi")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("at least 3 characters") })
    }

    @Test
    fun `max validation should work`() {
        val schema = basicStringSchema.max(5)
        
        // Should pass
        assertEquals("hi", schema.parse("hi"))
        
        // Should fail
        val result = schema.safeParse("hello world")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("at most 5 characters") })
    }

    @Test
    fun `length validation should work`() {
        val schema = basicStringSchema.length(3)
        
        // Should pass
        assertEquals("abc", schema.parse("abc"))
        
        // Should fail
        val result = schema.safeParse("ab")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("exactly 3 characters") })
    }

    @Test
    fun `email validation should work`() {
        val schema = basicStringSchema.email()
        
        // Should pass
        assertEquals("test@example.com", schema.parse("test@example.com"))
        assertEquals("user.name+tag@example.co.uk", schema.parse("user.name+tag@example.co.uk"))
        
        // Should fail
        val result = schema.safeParse("invalid-email")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Invalid email") })
    }

    @Test
    fun `url validation should work`() {
        val schema = basicStringSchema.url()
        
        // Should pass
        assertEquals("http://example.com", schema.parse("http://example.com"))
        assertEquals("https://example.com", schema.parse("https://example.com"))
        
        // Should fail
        val result = schema.safeParse("not-a-url")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Invalid URL") })
    }

    @Test
    fun `regex validation should work`() {
        val schema = basicStringSchema.regex(Regex("^[A-Z]{3}$"))
        
        // Should pass
        assertEquals("ABC", schema.parse("ABC"))
        
        // Should fail
        val result = schema.safeParse("abc")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("does not match required pattern") })
    }

    @Test
    fun `chained validations should work`() {
        val schema = basicStringSchema
            .min(3)
            .max(10)
            .regex(Regex("^[A-Za-z]*$"))
        
        // Should pass
        assertEquals("Hello", schema.parse("Hello"))
        
        // Should fail - too short
        val result1 = schema.safeParse("Hi")
        assertTrue(result1 is ZodResult.Failure)
        assertTrue(result1.error.errors.any { it.contains("at least 3 characters") })
        
        // Should fail - too long
        val result2 = schema.safeParse("ThisStringIsTooLong")
        assertTrue(result2 is ZodResult.Failure)
        assertTrue(result2.error.errors.any { it.contains("at most 10 characters") })
        
        // Should fail - contains numbers
        val result3 = schema.safeParse("Hello123")
        assertTrue(result3 is ZodResult.Failure)
        assertTrue(result3.error.errors.any { it.contains("does not match required pattern") })
    }

    @Test
    fun `startsWith validation should work`() {
        val schema = basicStringSchema.startsWith("Hello")
        
        // Should pass
        assertEquals("Hello World", schema.parse("Hello World"))
        
        // Should fail
        val result = schema.safeParse("Hi there")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must start with") })
    }

    @Test
    fun `endsWith validation should work`() {
        val schema = basicStringSchema.endsWith("World")
        
        // Should pass
        assertEquals("Hello World", schema.parse("Hello World"))
        
        // Should fail
        val result = schema.safeParse("Hello there")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must end with") })
    }

    @Test
    fun `includes validation should work`() {
        val schema = basicStringSchema.includes("test")
        
        // Should pass
        assertEquals("This is a test string", schema.parse("This is a test string"))
        
        // Should fail
        val result = schema.safeParse("Hello there")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("must contain") })
    }
}