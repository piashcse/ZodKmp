package com.piashcse.zodkmp

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Coverage for the less common branches of the public API: wrappers, suspend parsing,
 * collections, structured errors and JSON Schema conversion.
 */
class ZodCoverageTest {
    private fun failure(result: ZodResult<*>): ZodError {
        assertTrue(result is ZodResult.Failure, "expected a failure but was $result")
        return result.error
    }

    @Test
    fun `refine extension works on schemas without a typed refine`() =
        runTest {
            val schema = Zod.string().pipe(Zod.string()).refine({ it.startsWith("a") }, { "must start with a" })

            assertEquals("abc", schema.safeParse("abc").getOrNull())
            assertTrue(schema.safeParse("xyz") is ZodResult.Failure)
            assertTrue(schema.safeParse(1) is ZodResult.Failure)
            assertTrue(schema.safeParseSuspend("abc") is ZodResult.Success)
            assertTrue(schema.safeParseSuspend("xyz") is ZodResult.Failure)
        }

    @Test
    fun `map validates keys and values and supports suspend`() =
        runTest {
            val schema = Zod.map(Zod.string().min(2), Zod.number().min(0.0)).min(1).max(2)

            assertTrue(schema.safeParse(mapOf("ab" to 1)).isSuccess)
            val invalid = failure(schema.safeParse(mapOf("a" to -1)))
            assertEquals(2, invalid.issues.size)

            assertTrue(schema.safeParse(mapOf("ab" to 1, "cd" to 2)).isSuccess)
            assertTrue(schema.safeParse(emptyMap<String, Number>()) is ZodResult.Failure)
            assertTrue(schema.safeParse(mapOf("ab" to 1, "cd" to 2, "ef" to 3)) is ZodResult.Failure)

            assertTrue(schema.safeParseSuspend(mapOf("ab" to 1)) is ZodResult.Success)
            assertTrue(schema.safeParseSuspend(mapOf("a" to 1)) is ZodResult.Failure)
        }

    @Test
    fun `record supports value strict and loose key validation`() =
        runTest {
            val plain = Zod.record(Zod.number())
            assertTrue(plain.safeParse(mapOf("a" to 1)).isSuccess)
            assertTrue(plain.safeParse(mapOf("a" to "x")) is ZodResult.Failure)
            assertTrue(plain.safeParse(mapOf(1 to "x")) is ZodResult.Failure)

            val strict = Zod.record(Zod.string().min(2), Zod.number())
            assertTrue(strict.safeParse(mapOf("ab" to 1)).isSuccess)
            assertTrue(strict.safeParse(mapOf("a" to 1)) is ZodResult.Failure)

            val loose = Zod.looseRecord(Zod.string().min(2), Zod.number())
            assertTrue(loose.safeParse(mapOf("a" to 1)).isSuccess)
            assertTrue(loose.safeParse(mapOf("a" to "x")) is ZodResult.Failure)

            assertTrue(strict.safeParseSuspend(mapOf("ab" to 1)) is ZodResult.Success)
            assertTrue(strict.safeParseSuspend(mapOf("a" to 1)) is ZodResult.Failure)
        }

    @Test
    fun `tuple validates size and elements and supports suspend`() =
        runTest {
            val schema = Zod.tuple(Zod.string(), Zod.number())

            assertTrue(schema.safeParse(listOf("a", 1)).isSuccess)
            assertTrue(schema.safeParse(listOf("a")) is ZodResult.Failure)
            val invalid = failure(schema.safeParse(listOf(1, "a")))
            assertEquals(2, invalid.issues.size)
            assertTrue(schema.safeParseSuspend(listOf("a", 1)) is ZodResult.Success)
            assertTrue(schema.safeParseSuspend(listOf("a")) is ZodResult.Failure)
        }

    @Test
    fun `set constraints and suspend parsing work`() =
        runTest {
            val schema =
                Zod
                    .set(Zod.number())
                    .min(1)
                    .max(2)
                    .nonEmpty()
                    .refine({ it.isNotEmpty() })

            assertTrue(schema.safeParse(setOf(1)).isSuccess)
            assertTrue(schema.safeParse(emptySet<Int>()) is ZodResult.Failure)
            assertTrue(schema.safeParse(setOf(1, 2, 3)) is ZodResult.Failure)
            assertTrue(schema.safeParse(listOf(1)) is ZodResult.Failure)
            assertTrue(schema.safeParseSuspend(setOf(1)) is ZodResult.Success)
            assertTrue(schema.safeParseSuspend(setOf("wrong")) is ZodResult.Failure)
        }

    @Test
    fun `union suspend and failure branches work`() =
        runTest {
            val schema = Zod.union(Zod.string().min(2), Zod.number())
            assertTrue(schema.safeParse("ab") is ZodResult.Success)
            assertTrue(schema.safeParse("a") is ZodResult.Failure)
            assertTrue(schema.safeParseSuspend("ab") is ZodResult.Success)
            assertTrue(schema.safeParseSuspend(true) is ZodResult.Failure)
        }

    @Test
    fun `catch function fallback and suspend are covered`() =
        runTest {
            val schema = Zod.number().catch { input -> (input as? String)?.length?.toDouble() ?: -1.0 }
            assertEquals(3.0, schema.parse("abc"))
            assertEquals(-1.0, schema.parse(true))
            assertTrue(schema.safeParseSuspend("abc") is ZodResult.Success)
            assertEquals(3.0, schema.safeParseSuspend("abc").getOrNull())

            val absentCatch =
                Zod.objectSchema<Map<String, Any?>>({
                    field("value", Zod.string().catch { "fallback" })
                }) { it }
            assertEquals("fallback", absentCatch.parse(emptyMap<String, Any?>())["value"])
        }

    @Test
    fun `pipe suspend and failure branches work`() =
        runTest {
            val schema = Zod.string().pipe(Zod.coerce.number())
            assertEquals(42.0, schema.parse("42"))
            assertTrue(schema.safeParse(42) is ZodResult.Failure)
            assertTrue(schema.safeParseSuspend("42") is ZodResult.Success)
            assertTrue(schema.safeParseSuspend(42) is ZodResult.Failure)
        }

    @Test
    fun `transform exceptions and suspend are covered`() =
        runTest {
            val schema = Zod.string().transform { error("boom") }
            val error = failure(schema.safeParse("x"))
            assertTrue(error.issues.single().cause is IllegalStateException)

            val ok = Zod.string().transform { it.length }
            assertEquals(3, ok.safeParse("abc").getOrNull())
            assertEquals(3, ok.safeParseSuspend("abc").getOrNull())
            assertTrue(ok.safeParseSuspend(1) is ZodResult.Failure)
        }

    @Test
    fun `default supports suspend lambda and invalid non-null input`() =
        runTest {
            val schema = Zod.number().default { 42.0 }
            assertEquals(42.0, schema.parse(null))
            assertEquals(10.0, schema.parse(10))
            assertTrue(schema.safeParse("bad") is ZodResult.Failure)
            assertEquals(42.0, schema.safeParseSuspend(null).getOrNull())
            assertEquals(10.0, schema.safeParseSuspend(10).getOrNull())
            assertTrue(schema.safeParseSuspend("bad") is ZodResult.Failure)
        }

    @Test
    fun `json helpers convert and parse values`() =
        runTest {
            val schema =
                Zod.objectSchema<Map<String, Any?>>({
                    string("name")
                    boolean("active")
                    field("tags", Zod.array(Zod.string()))
                }) { it }

            val json =
                buildJsonObject {
                    put("name", "Ada")
                    put("active", true)
                    put("tags", buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive("a")) })
                }

            val result = schema.safeParseJson(json)
            assertTrue(result is ZodResult.Success)
            assertEquals("Ada", result.data["name"])
            assertEquals(true, result.data["active"])
            assertEquals(listOf("a"), result.data["tags"])

            assertEquals("Ada", schema.parseJson(json)["name"])
            assertTrue(schema.safeParseJsonSuspend(json) is ZodResult.Success)
            assertEquals("Ada", schema.parseJsonSuspend(json)["name"])
            assertEquals(null, JsonNull.toZodValue())
        }

    @Test
    fun `preprocess and describe suspend paths work`() =
        runTest {
            val preprocess = Zod.preprocess({ (it as? String)?.trim() }, Zod.string().min(2))
            assertTrue(preprocess.safeParseSuspend("  ab  ") is ZodResult.Success)
            assertTrue(preprocess.safeParseSuspend(" a ") is ZodResult.Failure)

            val described = Zod.string().min(2).describe("name")
            assertTrue(described.safeParseSuspend("ab") is ZodResult.Success)
            assertTrue(described.safeParseSuspend("a") is ZodResult.Failure)
        }

    @Test
    fun `long constraints and coercion are covered`() {
        assertTrue(
            Zod
                .long()
                .min(2)
                .safeParse(3L)
                .isSuccess,
        )
        assertTrue(Zod.long().min(2).safeParse(1L) is ZodResult.Failure)
        assertTrue(
            Zod
                .long()
                .negative()
                .safeParse(-1L)
                .isSuccess,
        )
        assertTrue(Zod.long().negative().safeParse(1L) is ZodResult.Failure)
        assertTrue(
            Zod
                .long()
                .nonNegative()
                .safeParse(0L)
                .isSuccess,
        )
        assertTrue(
            Zod
                .long()
                .nonPositive()
                .safeParse(0L)
                .isSuccess,
        )
        assertTrue(Zod.long().safeParse(1e30) is ZodResult.Failure)
        assertTrue(Zod.long().safeParse(Double.NaN) is ZodResult.Failure)
        assertTrue(Zod.long().safeParse(true) is ZodResult.Failure)
    }

    @Test
    fun `object helper create overload refine and parser failure are covered`() =
        runTest {
            val shape =
                linkedMapOf<String, ZodSchema<*>>(
                    "name" to Zod.string(),
                    "nickname" to Zod.string().optional(),
                )
            val schema = ZodObjectSchema.create(shape, { it }, strict = true, passthrough = true)
            assertTrue(schema.safeParse(mapOf("name" to "Ada")) is ZodResult.Success)
            assertTrue(schema.safeParse(mapOf("name" to "Ada", "extra" to 1)) is ZodResult.Failure)

            val withOptionalField =
                Zod.objectSchema<Map<String, Any?>>({
                    optionalField("nickname", Zod.string())
                }) { it }
            assertTrue(withOptionalField.safeParse(emptyMap<String, Any?>()) is ZodResult.Success)

            val refined =
                Zod
                    .objectSchema<Map<String, Any?>>({ string("name") }) { it }
                    .refine({ it["name"] == "ok" }, { "name must be ok" })
            assertTrue(refined.safeParse(mapOf("name" to "ok")) is ZodResult.Success)
            assertTrue(refined.safeParse(mapOf("name" to "nope")) is ZodResult.Failure)

            val throwing = Zod.objectSchema<Any>({ string("name") }) { error("constructor failed") }
            val error = failure(throwing.safeParse(mapOf("name" to "Ada")))
            assertTrue(
                error.issues
                    .single()
                    .message
                    .contains("constructor failed"),
            )

            val suspendSchema =
                Zod.objectSchema<Map<String, Any?>>({
                    field("name", Zod.string().refineSuspend({ it.isNotEmpty() }))
                }) { it }
            assertTrue(suspendSchema.safeParseSuspend(mapOf("name" to "a")) is ZodResult.Success)
            assertTrue(suspendSchema.safeParseSuspend(mapOf("name" to "")) is ZodResult.Failure)
        }

    @Test
    fun `intersection lazy nullable optional and instanceOf suspend paths work`() =
        runTest {
            val intersection = Zod.intersection(Zod.number().positive(), Zod.number().max(10.0))
            assertEquals(Pair(5.0, 5.0), intersection.parse(5))
            assertTrue(intersection.safeParseSuspend(5) is ZodResult.Success)
            assertTrue(intersection.safeParseSuspend(-1) is ZodResult.Failure)
            assertTrue(intersection.safeParseSuspend(20) is ZodResult.Failure)

            var node: ZodSchema<Map<String, Any?>>? = null
            node =
                Zod.lazy {
                    Zod.objectSchema<Map<String, Any?>>({ string("name") }) { it }
                }
            assertTrue(node!!.safeParseSuspend(mapOf("name" to "a")) is ZodResult.Success)

            assertTrue(Zod.string().nullable().safeParseSuspend(null) is ZodResult.Success)
            assertTrue(Zod.string().nullable().safeParseSuspend("a") is ZodResult.Success)
            assertTrue(Zod.string().optional().safeParseSuspend(null) is ZodResult.Success)
            assertTrue(Zod.instanceOf<String>().safeParseSuspend("a") is ZodResult.Success)
        }

    @Test
    fun `suspend refinement sync entry points throw`() {
        val schema = Zod.string().refineSuspend({ true })
        assertFailsWith<IllegalStateException> { schema.safeParse("x") }
        assertFailsWith<IllegalStateException> { schema.parse("x") }
    }

    @Test
    fun `structured error helpers are covered`() {
        val error = ZodError("a").addError("b").addErrors(listOf("c"))
        assertEquals(listOf("a", "b", "c"), error.errors)
        assertEquals("p: a", error.prepend(ZodPathSegment.Property("p")).errors.first())
        assertEquals("p[0]: a", error.prepend(listOf(ZodPathSegment.Property("p"), ZodPathSegment.Index(0))).errors.first())

        val form = ZodError("root").flatten()
        assertEquals(listOf("root"), form["formErrors"])
        assertTrue((form["fieldErrors"] as Map<*, *>).isEmpty())

        val exception = assertFailsWith<ZodValidationException> { Zod.string().parse(1) }
        assertTrue(exception.formatted().contains("Expected string"))
        assertTrue(exception.message!!.contains("Expected string"))
    }

    @Test
    fun `result map preserves failure`() {
        val failed = Zod.string().safeParse(1).map { it.length }
        assertTrue(failed is ZodResult.Failure)
        assertTrue(failed.isFailure)
        assertEquals(null, failed.getOrNull())
        assertFailsWith<ZodValidationException> { failed.getOrThrow() }
        assertEquals(-1, failed.fold({ it }, { -1 }))
    }

    @Test
    fun `url validator rejects malformed values`() {
        val schema = Zod.string().url()
        listOf(
            "",
            "example.com",
            "1http://example.com",
            "http://",
            "http:///path",
            "http://@/path",
            "http://[::1",
            "http://[::1]x",
            "http://[::1]:abc",
            "http://example.com:70000",
            "http://exa mple.com",
            "http://-example.com",
        ).forEach { value ->
            assertTrue(schema.safeParse(value) is ZodResult.Failure, "expected failure for '$value'")
        }
        assertTrue(schema.safeParse("http://[::1]:8080/a").isSuccess)
    }

    @Test
    fun `typed refinements on primitive wrappers are covered`() =
        runTest {
            assertTrue(
                Zod
                    .string()
                    .refine({ it.length > 2 })
                    .safeParse("abc")
                    .isSuccess,
            )
            assertTrue(Zod.string().refine({ it.length > 2 }).safeParse("a") is ZodResult.Failure)
            assertTrue(
                Zod
                    .number()
                    .refine({ it > 0 })
                    .safeParse(1)
                    .isSuccess,
            )
            assertTrue(Zod.number().refine({ it > 0 }).safeParse(-1) is ZodResult.Failure)
            assertTrue(
                Zod
                    .boolean()
                    .refine({ it })
                    .safeParse(true)
                    .isSuccess,
            )
            assertTrue(Zod.boolean().refine({ it }).safeParse(false) is ZodResult.Failure)
            assertTrue(
                Zod
                    .array(Zod.number())
                    .refine({ it.size > 1 })
                    .safeParse(listOf(1, 2))
                    .isSuccess,
            )
            assertTrue(Zod.array(Zod.number()).refine({ it.size > 1 }).safeParse(listOf(1)) is ZodResult.Failure)
            assertTrue(
                Zod
                    .date()
                    .refine({ it.year > 2000 })
                    .safeParse(LocalDate(2024, 1, 1))
                    .isSuccess,
            )
            assertTrue(Zod.date().refine({ it.year > 2000 }).safeParse(LocalDate(1999, 1, 1)) is ZodResult.Failure)
        }

    @Test
    fun `json schema conversion covers remaining node types`() {
        assertTrue(Zod.record(Zod.string()).toJsonSchema()["additionalProperties"] != null)
        assertTrue(Zod.map(Zod.string(), Zod.number()).toJsonSchema()["additionalProperties"] != null)
        assertEquals(2, (Zod.tuple(Zod.string(), Zod.number()).toJsonSchema()["items"] as kotlinx.serialization.json.JsonArray).size)
        assertTrue(Zod.intersection(Zod.number(), Zod.number()).toJsonSchema()["allOf"] != null)
        assertTrue(Zod.date().toJsonSchema()["format"] != null)
        assertTrue(Zod.nullType().toJsonSchema()["type"] != null)
        assertTrue(Zod.never().toJsonSchema()["not"] != null)
        assertTrue(Zod.void().toJsonSchema().isEmpty())
        assertTrue(Zod.any().toJsonSchema().isEmpty())
        assertTrue(Zod.unknown().toJsonSchema().isEmpty())
        assertTrue(Zod.instanceOf<String>().toJsonSchema().isEmpty())
        assertTrue(Zod.literal('x').toJsonSchema()["const"] != null)
        assertTrue(Zod.string().default("x").toJsonSchema()["type"] != null)
        assertTrue(Zod.string().catch("x").toJsonSchema()["type"] != null)
        assertTrue(Zod.string().transform { it }.toJsonSchema()["type"] != null)
        assertTrue(Zod.string().pipe(Zod.string()).toJsonSchema()["type"] != null)
        assertTrue(Zod.string().preprocess { it }.toJsonSchema()["type"] != null)
        assertTrue(Zod.string().describe("x").toJsonSchema()["type"] != null)
        assertTrue(Zod.string().nullable().toJsonSchema()["anyOf"] != null)

        val dog =
            Zod.objectSchema<Map<String, Any?>>({
                field("type", Zod.literal("dog"))
            }) { it }
        val union = Zod.discriminatedUnion<Map<String, Any?>>("type", dog)
        assertTrue(union.toJsonSchema()["oneOf"] != null)
    }

    @Test
    fun `json schema keeps object passthrough policy`() {
        val schema = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }.passthrough()
        assertEquals(
            true,
            schema
                .toJsonSchema()["additionalProperties"]!!
                .jsonPrimitive.content
                .toBoolean(),
        )
        val strip = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }.strip()
        assertTrue(strip.toJsonSchema()["additionalProperties"] == null)
        assertNotNull(schema.toJsonSchema()["properties"]!!.jsonObject)
    }

    @Test
    fun `discriminated union suspend and construction failures are covered`() =
        runTest {
            fun option(type: String) =
                Zod.objectSchema<Map<String, Any?>>({
                    field("type", Zod.literal(type))
                    string("value")
                }) { it }

            val schema = Zod.discriminatedUnion<Map<String, Any?>>("type", option("a"), option("b"))
            assertTrue(schema.safeParseSuspend(mapOf("type" to "a", "value" to "x")) is ZodResult.Success)
            assertTrue(schema.safeParseSuspend(mapOf("value" to "x")) is ZodResult.Failure)
            assertTrue(schema.safeParseSuspend(mapOf("type" to "c", "value" to "x")) is ZodResult.Failure)
            assertTrue(schema.safeParseSuspend(mapOf("type" to "a")) is ZodResult.Failure)
            assertTrue(schema.safeParse("not-an-object") is ZodResult.Failure)

            val withoutLiteral = Zod.objectSchema<Map<String, Any?>>({ string("value") }) { it }
            assertFailsWith<IllegalStateException> {
                Zod.discriminatedUnion<Map<String, Any?>>("type", withoutLiteral)
            }
            assertFailsWith<IllegalArgumentException> {
                Zod.discriminatedUnion<Map<String, Any?>>("type", emptyList())
            }
        }

    @Test
    fun `record suspend branches are covered`() =
        runTest {
            val plain = Zod.record(Zod.number())
            assertTrue(plain.safeParseSuspend(mapOf("a" to 1)) is ZodResult.Success)
            assertTrue(plain.safeParseSuspend(mapOf("a" to "x")) is ZodResult.Failure)
            assertTrue(plain.safeParseSuspend(mapOf(1 to "x")) is ZodResult.Failure)
            assertTrue(plain.safeParseSuspend("not-a-map") is ZodResult.Failure)

            val strict = Zod.record(Zod.string().min(2), Zod.number())
            assertTrue(strict.safeParseSuspend(mapOf("ab" to 1)) is ZodResult.Success)
            assertTrue(strict.safeParseSuspend(mapOf("a" to 1)) is ZodResult.Failure)

            val loose = Zod.looseRecord(Zod.string().min(2), Zod.number())
            assertTrue(loose.safeParseSuspend(mapOf("a" to 1)) is ZodResult.Success)
            assertTrue(loose.safeParseSuspend(mapOf("a" to "x")) is ZodResult.Failure)
        }

    @Test
    fun `object suspend branches include strict passthrough and parser errors`() =
        runTest {
            val strict = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }.strict()
            assertTrue(strict.safeParseSuspend(mapOf("a" to "x")) is ZodResult.Success)
            assertTrue(strict.safeParseSuspend(mapOf("a" to "x", "b" to "y")) is ZodResult.Failure)

            val pass = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }.passthrough()
            val passed = pass.safeParseSuspend(mapOf("a" to "x", "b" to "y"))
            assertTrue(passed is ZodResult.Success)
            assertEquals("y", passed.data["b"])

            assertTrue(pass.safeParseSuspend(mapOf(1 to "x")) is ZodResult.Failure)
            assertTrue(pass.safeParseSuspend("not-a-map") is ZodResult.Failure)

            val throwing = Zod.objectSchema<Any>({ string("a") }) { error("boom") }
            val error = failure(throwing.safeParseSuspend(mapOf("a" to "x")))
            assertTrue(
                error.issues
                    .single()
                    .message
                    .contains("boom"),
            )
        }

    @Test
    fun `literal overloads are covered`() {
        assertTrue(Zod.literal<String>().safeParse("x") is ZodResult.Failure)
        assertTrue(Zod.literal("a", "b").safeParse("a") is ZodResult.Success)
        assertTrue(Zod.literal("a", "b").safeParse("c") is ZodResult.Failure)
        assertEquals("x", ZodLiteral.schema("x").parse("x"))
        assertTrue(ZodLiteral.schema(*arrayOf("a")).safeParse("a") is ZodResult.Success)
    }

    @Test
    fun `default refinement messages are used`() =
        runTest {
            val extension = Zod.string().pipe(Zod.string()).refine({ false })
            assertTrue(
                extension.safeParse("x").let {
                    it is ZodResult.Failure &&
                        it.error.errors
                            .single()
                            .contains("Invalid value")
                },
            )

            val objectRefinement = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }.refine({ false })
            val error = failure(objectRefinement.safeParse(mapOf("a" to "x")))
            assertTrue(error.errors.single().contains("Invalid value"))

            val suspendFailure = Zod.string().pipe(Zod.string()).refine({ it == "ok" })
            assertTrue(suspendFailure.safeParseSuspend("no") is ZodResult.Failure)
        }

    @Test
    fun `leaf suspend defaults and public factories are covered`() =
        runTest {
            assertEquals("x", Zod.string().parseSuspend("x"))
            assertFailsWith<ZodValidationException> { Zod.string().parseSuspend(1) }
            assertEquals(1.0, Zod.number().safeParseSuspend(1).getOrNull())

            assertEquals(1.0, ZodDefault.schema(Zod.number(), { 1.0 }).parse(null))
            assertEquals("x", ZodCatch.schema(Zod.string(), { "x" }).parse(1))
            assertEquals(1.0, ZodTransform.schema(Zod.string()) { it.length.toDouble() }.parse("a"))
            assertEquals("a", ZodLiteral.schema("a").parse("a"))
            assertTrue(Zod.looseRecord(Zod.number()).safeParse(mapOf("a" to 1)).isSuccess)
        }

    @Test
    fun `suspend collection failures are covered`() =
        runTest {
            assertTrue(Zod.array(Zod.string()).safeParseSuspend(listOf(1)) is ZodResult.Failure)
            assertTrue(Zod.array(Zod.string()).safeParseSuspend("nope") is ZodResult.Failure)
            assertTrue(Zod.set(Zod.string()).safeParseSuspend(setOf(1)) is ZodResult.Failure)
            assertTrue(Zod.set(Zod.string()).safeParseSuspend(listOf("a")) is ZodResult.Failure)
            assertTrue(Zod.tuple(Zod.string()).safeParseSuspend(emptyList<String>()) is ZodResult.Failure)
            assertTrue(Zod.tuple(Zod.string()).safeParseSuspend(listOf(1)) is ZodResult.Failure)
            assertTrue(Zod.map(Zod.string(), Zod.number()).safeParseSuspend(mapOf(1 to "x")) is ZodResult.Failure)
            assertTrue(Zod.map(Zod.string(), Zod.number()).safeParseSuspend(mapOf("a" to "x")) is ZodResult.Failure)
            assertTrue(Zod.map(Zod.string(), Zod.number()).safeParseSuspend("nope") is ZodResult.Failure)
        }

    @Test
    fun `suspend wrappers report base failures`() =
        runTest {
            val refinement = Zod.string().pipe(Zod.string()).refine({ true })
            assertTrue(refinement.safeParseSuspend(1) is ZodResult.Failure)

            val suspend = Zod.string().refineSuspend({ false })
            val error = failure(suspend.safeParseSuspend("x"))
            assertTrue(error.errors.single().contains("Invalid value"))

            val discriminated =
                Zod.discriminatedUnion<Map<String, Any?>>(
                    "type",
                    Zod.objectSchema<Map<String, Any?>>({
                        field("type", Zod.literal("a"))
                        string("value")
                    }) { it },
                )
            assertTrue(discriminated.safeParseSuspend("not-an-object") is ZodResult.Failure)
            assertTrue(discriminated.safeParseSuspend(mapOf("type" to "a")) is ZodResult.Failure)
        }

    @Test
    fun `boolean coercion and json literal primitives are covered`() {
        assertTrue(Zod.boolean().safeParse("true") is ZodResult.Failure)
        assertTrue(Zod.boolean().safeParse(1) is ZodResult.Failure)
        assertTrue(Zod.coerce.boolean().safeParse("TRUE") is ZodResult.Success)

        val primitive = kotlinx.serialization.json.JsonPrimitive("x")
        assertEquals(primitive, Zod.literal(primitive).toJsonSchema()["const"])
    }
}
