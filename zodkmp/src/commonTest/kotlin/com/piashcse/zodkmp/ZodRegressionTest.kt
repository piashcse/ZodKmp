package com.piashcse.zodkmp

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Regression tests for the correctness issues fixed in 1.4.0.
 */
class ZodRegressionTest {
    @Test
    fun `url accepts query strings, ports, localhost and mixed case`() {
        val schema = Zod.string().url()
        listOf(
            "https://example.com?q=1&lang=en",
            "http://localhost:3000",
            "HTTPS://EXAMPLE.COM",
            "https://example.com/path#fragment",
            "https://user:pass@example.com:8443/a?b=c",
        ).forEach { url ->
            assertTrue(schema.safeParse(url) is ZodResult.Success, "expected valid URL: $url")
        }
        assertTrue(schema.safeParse("example.com") is ZodResult.Failure)
        assertTrue(schema.safeParse("not a url") is ZodResult.Failure)
    }

    @Test
    fun `email rejects malformed addresses`() {
        val schema = Zod.string().email()
        assertTrue(schema.safeParse("user.name+tag@example.co.uk") is ZodResult.Success)
        assertTrue(schema.safeParse("a@b..com") is ZodResult.Failure)
        assertTrue(schema.safeParse("user@localhost") is ZodResult.Failure)
    }

    @Test
    fun `regex matches substrings like Zod`() {
        assertTrue(Zod.string().regex(Regex("abc")).safeParse("xabcx") is ZodResult.Success)
    }

    @Test
    fun `string checks run in declaration order`() {
        // min sees the untrimmed value, then trim is applied
        assertEquals(
            "ab",
            Zod
                .string()
                .min(4)
                .trim()
                .parse("  ab  "),
        )
        // trim runs first, so min sees the trimmed value
        assertTrue(
            Zod
                .string()
                .trim()
                .min(4)
                .safeParse("  ab  ") is ZodResult.Failure,
        )
    }

    @Test
    fun `string length counts code points`() {
        assertEquals("\uD83D\uDE00", Zod.string().length(1).parse("\uD83D\uDE00"))
        assertTrue(Zod.string().length(1).safeParse("ab") is ZodResult.Failure)
    }

    @Test
    fun `emoji accepts keycap sequences and rejects bare digits`() {
        assertTrue(Zod.string().emoji().safeParse("1\uFE0F\u20E3") is ZodResult.Success)
        assertTrue(Zod.string().emoji().safeParse("1") is ZodResult.Failure)
    }

    @Test
    fun `datetime rejects date-only values but accepts offsets`() {
        val schema = Zod.string().datetime()
        assertTrue(schema.safeParse("2023-01-01") is ZodResult.Failure)
        assertTrue(schema.safeParse("2023-01-01T12:00:00") is ZodResult.Success)
        assertTrue(schema.safeParse("2023-01-01T12:00:00Z") is ZodResult.Success)
        assertTrue(schema.safeParse("2023-01-01T12:00:00+05:30") is ZodResult.Success)
    }

    @Test
    fun `cuid2 and nanoid enforce their default lengths`() {
        assertTrue(Zod.string().cuid2().safeParse("abc") is ZodResult.Failure)
        assertTrue(Zod.string().nanoid().safeParse("a") is ZodResult.Failure)
        assertTrue(Zod.string().cuid2(3).safeParse("abc") is ZodResult.Success)
        assertTrue(Zod.string().nanoid(3).safeParse("a_b") is ZodResult.Success)
    }

    @Test
    fun `multipleOf handles decimal steps and rejects zero`() {
        assertTrue(Zod.number().multipleOf(0.1).safeParse(0.3) is ZodResult.Success)
        assertTrue(Zod.number().multipleOf(0.1).safeParse(0.31) is ZodResult.Failure)
        assertFailsWith<IllegalArgumentException> { Zod.number().multipleOf(0.0) }
    }

    @Test
    fun `strict and passthrough are mutually exclusive`() {
        val strictThenPassthrough =
            Zod
                .objectSchema<Map<String, Any?>>({ string("a") }) { it }
                .strict()
                .passthrough()
        val passthroughResult = strictThenPassthrough.safeParse(mapOf("a" to "x", "b" to "y"))
        assertTrue(passthroughResult is ZodResult.Success, "last call wins: passthrough")

        val passthroughThenStrict =
            Zod
                .objectSchema<Map<String, Any?>>({ string("a") }) { it }
                .passthrough()
                .strict()
        assertTrue(passthroughThenStrict.safeParse(mapOf("a" to "x", "b" to "y")) is ZodResult.Failure)

        val stripped =
            Zod
                .objectSchema<Map<String, Any?>>({ string("a") }) { it }
                .passthrough()
                .strip()
        val result = stripped.safeParse(mapOf("a" to "x", "b" to "y"))
        assertTrue(result is ZodResult.Success)
        assertTrue(!result.data.containsKey("b"))
    }

    @Test
    fun `merge keeps passthrough from either side`() {
        val a = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }
        val b = Zod.objectSchema<Map<String, Any?>>({ string("b") }) { it }.passthrough()
        val result = a.merge(b).safeParse(mapOf("a" to "x", "b" to "y", "extra" to 1))
        assertTrue(result is ZodResult.Success)
        assertEquals(1, result.data["extra"])
    }

    @Test
    fun `deepPartial preserves collection constraints`() {
        val schema =
            Zod
                .objectSchema<Map<String, Any?>>({
                    field("tags", Zod.array(Zod.string()).min(2).unique())
                }) { it }
                .deepPartial()

        assertTrue(schema.safeParse(emptyMap<String, Any?>()) is ZodResult.Success)
        assertTrue(schema.safeParse(mapOf("tags" to listOf("a"))) is ZodResult.Failure)
        assertTrue(schema.safeParse(mapOf("tags" to listOf("a", "a"))) is ZodResult.Failure)
        assertTrue(schema.safeParse(mapOf("tags" to listOf("a", "b"))) is ZodResult.Success)
    }

    @Test
    fun `deepPartial still makes nested object properties optional`() {
        val inner = Zod.objectSchema<Map<String, Any?>>({ string("city") }) { it }
        val schema = Zod.objectSchema<Map<String, Any?>>({ field("address", inner) }) { it }.deepPartial()
        assertTrue(schema.safeParse(mapOf("address" to emptyMap<String, Any?>())) is ZodResult.Success)
    }

    @Test
    fun `optional shape helpers are callable without explicit type arguments`() {
        val schema =
            Zod.objectSchema<Map<String, Any?>>({
                optionalString("nickname")
                optionalNumber("age")
                optionalBoolean("active")
            }) { it }

        assertTrue(schema.safeParse(emptyMap<String, Any?>()) is ZodResult.Success)
    }

    @Test
    fun `optional followed by default keeps explicit null`() {
        val schema = Zod.string().optional().default("fallback")
        assertEquals(null, schema.parse(null))
        assertEquals("fallback", schema.parse(ZodAbsent))
    }

    @Test
    fun `missing required keys are reported as Required`() {
        val schema = Zod.objectSchema<Map<String, Any?>>({ string("name") }) { it }
        val result = schema.safeParse(emptyMap<String, Any?>())
        assertTrue(result is ZodResult.Failure)
        assertTrue(result.error.errors.any { it.contains("Required") })
    }

    @Test
    fun `objects and records reject non-string keys`() {
        val objectSchema = Zod.objectSchema<Map<String, Any?>>({ string("1") }) { it }
        assertTrue(objectSchema.safeParse(mapOf(1 to "x")) is ZodResult.Failure)
        assertTrue(Zod.record(Zod.string()).safeParse(mapOf(1 to "x")) is ZodResult.Failure)
    }

    @Test
    fun `literal matches numeric values across Kotlin numeric types`() {
        assertEquals(1, Zod.literal(1).parse(1L))
        assertTrue(Zod.literal(1.5).safeParse(1.5f) is ZodResult.Success)
        assertTrue(Zod.literal(1).safeParse(2L) is ZodResult.Failure)
    }

    @Test
    fun `enum name coercion ignores non-enum values instead of crashing`() {
        val schema = ZodEnum.schema(listOf("a", "b"), coerceByEnumName = true)
        assertTrue(schema.safeParse("zzz") is ZodResult.Failure)
    }

    @Test
    fun `discriminated union rejects duplicate discriminators`() {
        fun option(type: String) =
            Zod.objectSchema<Map<String, Any?>>({
                field("type", Zod.literal(type))
            }) { it }

        assertFailsWith<IllegalArgumentException> {
            Zod.discriminatedUnion<Map<String, Any?>>("type", option("a"), option("a"))
        }
    }

    @Test
    fun `discriminated union does not guess among options`() {
        val a =
            Zod.objectSchema<Map<String, Any?>>({
                field("type", Zod.literal("a"))
                string("value")
            }) { it }
        val schema = Zod.discriminatedUnion<Map<String, Any?>>("type", a)
        assertTrue(schema.safeParse(mapOf("type" to "b", "value" to "x")) is ZodResult.Failure)
    }

    @Test
    fun `object intersection merges shapes into a single map`() {
        val a = Zod.objectSchema<Map<String, Any?>>({ string("a") }) { it }
        val b = Zod.objectSchema<Map<String, Any?>>({ string("b") }) { it }
        val result = Zod.intersection(a, b).parse(mapOf("a" to "x", "b" to "y"))
        assertEquals(mapOf("a" to "x", "b" to "y"), result)
    }

    @Test
    fun `array accepts primitive arrays`() {
        assertEquals(listOf(1.0, 2.0), Zod.array(Zod.number()).parse(intArrayOf(1, 2)))
    }

    @Test
    fun `date factory is available and string parsing is opt-in`() {
        val date = LocalDateTime(2023, 1, 1, 12, 0)
        assertEquals(date, Zod.date().parse(date))
        assertTrue(Zod.date().safeParse("2023-01-01T12:00:00") is ZodResult.Failure)
        assertTrue(Zod.coerce.date().safeParse("2023-01-01T12:00:00") is ZodResult.Success)
    }

    @Test
    fun `suspend refinements compose through objects, arrays and wrappers`() =
        runTest {
            val nested =
                Zod.objectSchema<Map<String, Any?>>({
                    field("name", Zod.string().refineSuspend({ it != "blocked" }))
                    field("tags", Zod.array(Zod.string().refineSuspend({ it.isNotEmpty() })))
                }) { it }

            val ok = nested.safeParseSuspend(mapOf("name" to "ok", "tags" to listOf("a")))
            assertTrue(ok is ZodResult.Success)

            val blocked = nested.safeParseSuspend(mapOf("name" to "blocked", "tags" to listOf("a")))
            assertTrue(blocked is ZodResult.Failure)

            val optional = Zod.string().refineSuspend({ it != "blocked" }).optional()
            assertTrue(optional.safeParseSuspend(null) is ZodResult.Success)
            assertTrue(optional.safeParseSuspend("blocked") is ZodResult.Failure)
        }
}
