package com.piashcse.zodkmp.codegen

import com.piashcse.zodkmp.ZodResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodegenTest {

    private val valid = mapOf(
        "name" to "Ada",
        "age" to 30,
        "score" to 9.5,
        "role" to "ADMIN",
        "nickname" to null,
        "tags" to listOf("math", "code"),
        "address" to mapOf("city" to "London"),
    )

    @Test
    fun `generated schema parses a model`() {
        val result = UserZod.schema.safeParse(valid)
        assertTrue(result is ZodResult.Success, "expected success but was $result")
        val user = result.data
        assertEquals("Ada", user.name)
        assertEquals(30, user.age)
        assertEquals(9.5, user.score)
        assertEquals(Role.ADMIN, user.role)
        assertEquals(null, user.nickname)
        assertEquals(listOf("math", "code"), user.tags)
        assertEquals(Address("London"), user.address)
    }

    @Test
    fun `nullable properties may be missing`() {
        val input = valid - "address"
        val result = UserZod.schema.safeParse(input)
        assertTrue(result is ZodResult.Success)
        assertEquals(null, result.data.address)
    }

    @Test
    fun `strict annotation rejects unknown keys`() {
        val result = UserZod.schema.safeParse(valid + ("extra" to 1))
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `unsupported property types fall back to any with a cast`() {
        val result = MiscZod.schema.safeParse(mapOf("code" to 7.toShort(), "marker" to 'x'))
        assertTrue(result is ZodResult.Success)
        assertEquals(7.toShort(), result.data.code)
        assertEquals('x', result.data.marker)
    }

    @Test
    fun `generated schema rejects invalid property values`() {
        val result = UserZod.schema.safeParse(valid + ("age" to 3.5))
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("age") })
    }
}
