package com.piashcse.zodkmp

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodDateTest {
    @Test
    fun `parse should succeed with valid LocalDateTime`() {
        val date = LocalDateTime(2023, Month.JANUARY, 1, 12, 0, 0)
        val result = ZodDate.schema().parse(date)
        assertEquals(date, result)
    }

    @Test
    fun `parse should succeed with valid date string`() {
        val result = ZodDate.schema().parse("2023-01-01")
        val expected = LocalDateTime(2023, Month.JANUARY, 1, 0, 0, 0)
        assertEquals(expected, result)
    }

    @Test
    fun `parse should succeed with valid datetime string`() {
        val result = ZodDate.schema().parse("2023-01-01T12:30:45")
        val expected = LocalDateTime(2023, Month.JANUARY, 1, 12, 30, 45)
        assertEquals(expected, result)
    }

    @Test
    fun `parse should throw with invalid input`() {
        assertFailsWith<IllegalArgumentException> {
            ZodDate.schema().parse("not-a-date")
        }
        
        assertFailsWith<IllegalArgumentException> {
            ZodDate.schema().parse(123)
        }
    }

    @Test
    fun `safeParse should return Success with valid LocalDateTime`() {
        val date = LocalDateTime(2023, Month.JANUARY, 1, 12, 0, 0)
        val result = ZodDate.schema().safeParse(date)
        assertTrue(result is ZodResult.Success<*>)
        assertEquals(date, result.data)
    }

    @Test
    fun `safeParse should return Success with valid date string`() {
        val result = ZodDate.schema().safeParse("2023-01-01")
        assertTrue(result is ZodResult.Success<*>)
        val expected = LocalDateTime(2023, Month.JANUARY, 1, 0, 0, 0)
        assertEquals(expected, result.data)
    }

    @Test
    fun `safeParse should return Failure with invalid input`() {
        val result = ZodDate.schema().safeParse("not-a-date")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("Expected date"))
    }

    @Test
    fun `min validation should work`() {
        val schema = ZodDate.schema().min(LocalDateTime(2023, Month.JANUARY, 1, 0, 0, 0))
        
        // Should pass
        val validDate = LocalDateTime(2023, Month.JANUARY, 2, 12, 0, 0)
        assertEquals(validDate, schema.parse(validDate))
        
        // Should fail
        val invalidDate = LocalDateTime(2022, Month.DECEMBER, 31, 12, 0, 0)
        val result = schema.safeParse(invalidDate)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("greater than or equal to") })
    }

    @Test
    fun `max validation should work`() {
        val schema = ZodDate.schema().max(LocalDateTime(2023, Month.JANUARY, 31, 23, 59, 59))
        
        // Should pass
        val validDate = LocalDateTime(2023, Month.JANUARY, 15, 12, 0, 0)
        assertEquals(validDate, schema.parse(validDate))
        
        // Should fail
        val invalidDate = LocalDateTime(2023, Month.FEBRUARY, 1, 12, 0, 0)
        val result = schema.safeParse(invalidDate)
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("less than or equal to") })
    }

    @Test
    fun `chained date validations should work`() {
        val schema = ZodDate.schema()
            .min(LocalDateTime(2023, Month.JANUARY, 1, 0, 0, 0))
            .max(LocalDateTime(2023, Month.DECEMBER, 31, 23, 59, 59))
        
        // Should pass
        val validDate = LocalDateTime(2023, Month.JULY, 1, 12, 0, 0)
        assertEquals(validDate, schema.parse(validDate))
        
        // Should fail - too early
        val result1 = schema.safeParse(LocalDateTime(2022, Month.JANUARY, 1, 12, 0, 0))
        assertTrue(result1 is ZodResult.Failure)
        
        // Should fail - too late
        val result2 = schema.safeParse(LocalDateTime(2024, Month.JANUARY, 1, 12, 0, 0))
        assertTrue(result2 is ZodResult.Failure)
    }
}