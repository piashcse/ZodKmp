package com.piashcse.zodkmp

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodSuspendRefinementTest {

    @Test
    fun `refineSuspend should run base validation synchronously through safeParseSuspend`() = runTest {
        val schema = Zod.string().min(3).refineSuspend({ it != "blocked" })
        val result = schema.safeParseSuspend("hello")
        assertTrue(result is ZodResult.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun `refineSuspend should reject values failing the suspend refinement`() = runTest {
        val schema = Zod.string().refineSuspend({ it != "blocked" }, { "value is blocked" })
        val result = schema.safeParseSuspend("blocked")
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.first().contains("blocked"))
    }

    @Test
    fun `refineSuspend should surface base schema failures`() = runTest {
        val schema = Zod.number().positive().refineSuspend({ true })
        val result = schema.safeParseSuspend(-1)
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `parseSuspend should return value on success`() = runTest {
        val schema = Zod.string().refineSuspend({ true })
        assertEquals("ok", schema.parseSuspend("ok"))
    }

    @Test
    fun `safeParseSuspend default implementation should work for plain schemas`() = runTest {
        val schema = Zod.number().gt(5.0)
        val result = schema.safeParseSuspend(10)
        assertTrue(result is ZodResult.Success)
    }
}
