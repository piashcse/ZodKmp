package com.piashcse.zodkmp.codegen

import com.piashcse.zodkmp.ZodResult
import com.piashcse.zodkmp.codegen.other.External
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodegenTypesTest {

    private val valid = mapOf(
        "longValue" to 9_000_000_000L,
        "floatValue" to 1.5,
        "enabled" to true,
        "ages" to listOf(1, 2),
        "roles" to listOf("ADMIN", "USER"),
        "addresses" to listOf(mapOf("city" to "Paris")),
        "maybeCount" to 3,
        "maybeLong" to 7L,
        "maybeFloat" to 2.5,
        "maybeTags" to listOf("x"),
        "external" to mapOf("id" to 42L),
    )

    @Test
    fun `generated schema handles all supported types`() {
        val result = TypesZod.schema.safeParse(valid)
        assertTrue(result is ZodResult.Success, "expected success but was $result")
        val value = result.data
        assertEquals(9_000_000_000L, value.longValue)
        assertEquals(1.5f, value.floatValue)
        assertEquals(true, value.enabled)
        assertEquals(listOf(1, 2), value.ages)
        assertEquals(listOf(Role.ADMIN, Role.USER), value.roles)
        assertEquals(listOf(Address("Paris")), value.addresses)
        assertEquals(3, value.maybeCount)
        assertEquals(7L, value.maybeLong)
        assertEquals(2.5f, value.maybeFloat)
        assertEquals(listOf("x"), value.maybeTags)
        assertEquals(External(42L), value.external)
    }

    @Test
    fun `nullable collections and models may be missing`() {
        val input = valid - "maybeCount" - "maybeLong" - "maybeFloat" - "maybeTags" - "external"
        val result = TypesZod.schema.safeParse(input)
        assertTrue(result is ZodResult.Success)
        assertEquals(null, result.data.maybeCount)
        assertEquals(null, result.data.maybeLong)
        assertEquals(null, result.data.maybeFloat)
        assertEquals(null, result.data.maybeTags)
        assertEquals(null, result.data.external)
    }

    @Test
    fun `list conversions reject invalid element values`() {
        assertTrue(TypesZod.schema.safeParse(valid + ("ages" to listOf(1.5))) is ZodResult.Failure)
        assertTrue(TypesZod.schema.safeParse(valid + ("roles" to listOf("NOPE"))) is ZodResult.Failure)
    }
}
