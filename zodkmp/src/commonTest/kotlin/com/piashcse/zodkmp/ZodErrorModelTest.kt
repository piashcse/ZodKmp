package com.piashcse.zodkmp

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZodErrorModelTest {
    private fun failure(result: ZodResult<*>): ZodError {
        assertTrue(result is ZodResult.Failure, "expected a failure but was $result")
        return result.error
    }

    @Test
    fun `issues carry the full property path`() {
        val address =
            Zod.objectSchema<Map<String, Any?>>({
                string("city", Zod.string().min(3))
            }) { it }
        val user =
            Zod.objectSchema<Map<String, Any?>>({
                string("name")
                field("address", address)
            }) { it }

        val error = failure(user.safeParse(mapOf("name" to "Ada", "address" to mapOf("city" to "x"))))

        assertEquals(
            listOf(ZodPathSegment.Property("address"), ZodPathSegment.Property("city")),
            error.issues.single().path,
        )
        assertEquals(ZodIssueCode.TOO_SMALL, error.issues.single().code)
        assertTrue(error.errors.single().startsWith("address.city: "))
    }

    @Test
    fun `array issues carry index paths`() {
        val schema =
            Zod.objectSchema<Map<String, Any?>>({
                field("tags", Zod.array(Zod.string().min(2)))
            }) { it }

        val error = failure(schema.safeParse(mapOf("tags" to listOf("ok", "x"))))

        assertEquals(
            listOf(ZodPathSegment.Property("tags"), ZodPathSegment.Index(1)),
            error.issues.single().path,
        )
        assertEquals("tags[1]: String must be at least 2 characters long", error.errors.single())
    }

    @Test
    fun `missing properties produce required issues`() {
        val schema = Zod.objectSchema<Map<String, Any?>>({ string("name") }) { it }
        val error = failure(schema.safeParse(emptyMap<String, Any?>()))
        assertEquals(ZodIssueCode.REQUIRED, error.issues.single().code)
        assertEquals(listOf(ZodPathSegment.Property("name")), error.issues.single().path)
    }

    @Test
    fun `parse throws ZodValidationException with structured error`() {
        val exception =
            assertFailsWith<ZodValidationException> {
                Zod.objectSchema<Map<String, Any?>>({ string("name") }) { it }.parse(emptyMap<String, Any?>())
            }
        assertEquals(
            ZodIssueCode.REQUIRED,
            exception.error.issues
                .single()
                .code,
        )
        assertTrue(exception.message!!.contains("name"))
    }

    @Test
    fun `format validators use invalid format issues`() {
        val error = failure(Zod.string().email().safeParse("nope"))
        assertEquals(ZodIssueCode.INVALID_FORMAT, error.issues.single().code)
    }

    @Test
    fun `union and literal failures have typed codes`() {
        assertEquals(
            ZodIssueCode.INVALID_UNION,
            failure(Zod.union(Zod.string(), Zod.number()).safeParse(true)).issues.single().code,
        )
        assertEquals(
            ZodIssueCode.INVALID_LITERAL,
            failure(Zod.literal("x").safeParse("y")).issues.single().code,
        )
    }

    @Test
    fun `flatten groups errors by top level field`() {
        val schema =
            Zod.objectSchema<Map<String, Any?>>({
                string("name", Zod.string().min(3))
                number("age", Zod.number().min(18.0))
            }) { it }

        val flat = failure(schema.safeParse(mapOf("name" to "a", "age" to 10))).flatten()

        @Suppress("UNCHECKED_CAST")
        val fieldErrors = flat["fieldErrors"] as Map<String, List<String>>
        assertTrue(fieldErrors.containsKey("name"))
        assertTrue(fieldErrors.containsKey("age"))
        assertEquals(emptyList<String>(), flat["formErrors"])
    }

    @Test
    fun `result helpers work`() {
        val success = Zod.string().safeParse("hello")
        assertEquals("hello", success.getOrNull())
        assertEquals("HELLO", success.map { it.uppercase() }.getOrNull())
        assertEquals(5, success.fold({ it.length }, { -1 }))
        assertTrue(success.isSuccess)

        val failed = Zod.string().safeParse(1)
        assertEquals(null, failed.getOrNull())
        assertTrue(failed.isFailure)
        assertFailsWith<ZodValidationException> { failed.getOrThrow() }
        assertEquals(-1, failed.fold({ it.length }, { -1 }))
    }

    @Test
    fun `refine preserves the concrete schema type`() {
        val schema =
            Zod
                .string()
                .refine({ it.startsWith("a") }, { "must start with a" })
                .min(3)
        assertTrue(schema.safeParse("abc") is ZodResult.Success)
        assertTrue(schema.safeParse("ab") is ZodResult.Failure, "min(3) must still be chainable")
        assertTrue(schema.safeParse("xyz") is ZodResult.Failure)

        val objectSchema =
            Zod
                .objectSchema<Map<String, Any?>>({ string("a") }) { it }
                .refine({ it.containsKey("a") }, { "a is required" })
                .strict()
        assertTrue(objectSchema.safeParse(mapOf("a" to "x")) is ZodResult.Success)
        assertTrue(objectSchema.safeParse(mapOf("a" to "x", "b" to "y")) is ZodResult.Failure)
    }

    @Test
    fun `lazy supports recursive schemas`() {
        lateinit var node: ZodSchema<Map<String, Any?>>
        node =
            Zod.lazy {
                Zod.objectSchema<Map<String, Any?>>({
                    string("name")
                    field("children", Zod.array(node))
                }) { it }
            }

        val input =
            mapOf(
                "name" to "root",
                "children" to
                    listOf(
                        mapOf("name" to "child", "children" to emptyList<Any?>()),
                    ),
            )
        assertTrue(node.safeParse(input) is ZodResult.Success)
    }

    @Test
    fun `error map localizes messages while keeping paths`() {
        ZodConfig.errorMap = { issue ->
            when (issue.code) {
                ZodIssueCode.REQUIRED -> "Requerido"
                ZodIssueCode.INVALID_TYPE -> "Tipo no válido"
                else -> null
            }
        }
        try {
            val schema = Zod.objectSchema<Map<String, Any?>>({ string("name") }) { it }
            val error = failure(schema.safeParse(emptyMap<String, Any?>()))
            assertEquals("name: Requerido", error.errors.single())
        } finally {
            ZodConfig.reset()
        }
    }

    @Test
    fun `json elements can be parsed directly`() {
        val schema =
            Zod.objectSchema<Map<String, Any?>>({
                string("name")
                number("age")
            }) { it }

        val json =
            buildJsonObject {
                put("name", "Ada")
                put("age", 30)
            }

        val result = schema.safeParseJson(json)
        assertTrue(result is ZodResult.Success)
        assertEquals("Ada", result.data["name"])
        assertEquals(30.0, result.data["age"])
    }
}
