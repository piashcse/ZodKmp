package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodObjectOperationsTest {

    private fun baseObject(): ZodObjectSchema<Map<String, Any?>> {
        val builder = ZodObjectShapeBuilder()
        builder.string("name")
        builder.number("age")
        builder.boolean("active")
        return ZodObjectSchema.create(
            shape = builder.shape,
            parser = { it }
        )
    }

    private fun nestedObject(): ZodObjectSchema<Map<String, Any?>> {
        val addressBuilder = ZodObjectShapeBuilder()
        addressBuilder.string("city")
        addressBuilder.number("zip")
        val address = ZodObjectSchema.create(shape = addressBuilder.shape, parser = { it })

        val userBuilder = ZodObjectShapeBuilder()
        userBuilder.string("name")
        userBuilder.field("address", address)
        return ZodObjectSchema.create(shape = userBuilder.shape, parser = { it })
    }

    @Test
    fun `pick should only validate selected keys`() {
        val schema = baseObject().pick("name", "age")
        val result = schema.safeParse(mapOf("name" to "John", "age" to 30, "active" to true))
        assertTrue(result is ZodResult.Success)
        assertEquals(mapOf("name" to "John", "age" to 30.0), result.data)
    }

    @Test
    fun `omit should exclude keys`() {
        val schema = baseObject().omit("active")
        val result = schema.safeParse(mapOf("name" to "John", "age" to 30, "active" to true))
        assertTrue(result is ZodResult.Success)
        assertEquals(mapOf("name" to "John", "age" to 30.0), result.data)
    }

    @Test
    fun `partial should make all fields optional`() {
        val schema = baseObject().partial()
        assertTrue(schema.safeParse(mapOf("name" to "John")) is ZodResult.Success)
        assertTrue(schema.safeParse(emptyMap<String, Any?>()) is ZodResult.Success)
        assertTrue(schema.safeParse(mapOf("name" to "John", "age" to "not-number")) is ZodResult.Failure)
    }

    @Test
    fun `deepPartial should make nested objects optional too`() {
        val schema = nestedObject().deepPartial()
        assertTrue(schema.safeParse(mapOf("name" to "John")) is ZodResult.Success)
        assertTrue(schema.safeParse(mapOf("name" to "John", "address" to mapOf("city" to "NYC"))) is ZodResult.Success)
        assertTrue(schema.safeParse(emptyMap<String, Any?>()) is ZodResult.Success)
    }

    @Test
    fun `merge should combine two objects`() {
        val a = baseObject()
        val bBuilder = ZodObjectShapeBuilder()
        bBuilder.string("email")
        val b = ZodObjectSchema.create(shape = bBuilder.shape, parser = { it })

        val merged = a.merge(b)
        val result = merged.safeParse(mapOf("name" to "John", "age" to 30, "active" to true, "email" to "j@x.com"))
        assertTrue(result is ZodResult.Success)
        assertEquals("j@x.com", result.data["email"])
        assertEquals("John", result.data["name"])
    }

    @Test
    fun `merge should fail on key conflicts`() {
        val a = baseObject()
        val bBuilder = ZodObjectShapeBuilder()
        bBuilder.string("name")
        val b = ZodObjectSchema.create(shape = bBuilder.shape, parser = { it })

        val thrown = runCatching { a.merge(b) }
        assertTrue(thrown.isFailure)
    }

    @Test
    fun `and infix should merge objects`() {
        val a = baseObject()
        val bBuilder = ZodObjectShapeBuilder()
        bBuilder.number("score")
        val b = ZodObjectSchema.create(shape = bBuilder.shape, parser = { it })

        val merged = a and b
        val result = merged.safeParse(mapOf("name" to "John", "age" to 30, "active" to true, "score" to 99))
        assertTrue(result is ZodResult.Success)
        assertEquals(99.0, result.data["score"])
    }

    @Test
    fun `extend should override conflicting keys`() {
        val a = baseObject()
        val bBuilder = ZodObjectShapeBuilder()
        bBuilder.number("age")
        bBuilder.string("email")
        val b = ZodObjectSchema.create(shape = bBuilder.shape, parser = { it })

        val extended = a.extend(b)
        val result = extended.safeParse(mapOf("name" to "John", "age" to 30, "active" to true, "email" to "j@x.com"))
        assertTrue(result is ZodResult.Success)
        assertEquals(30.0, result.data["age"])
    }

    @Test
    fun `passthrough should keep extra keys in output`() {
        val schema = baseObject().passthrough()
        val result = schema.safeParse(mapOf("name" to "John", "age" to 30, "active" to true, "extra" to "kept"))
        assertTrue(result is ZodResult.Success)
        assertEquals("kept", result.data["extra"])
    }

    @Test
    fun `strip should drop extra keys by default`() {
        val schema = baseObject().strip()
        val result = schema.safeParse(mapOf("name" to "John", "age" to 30, "active" to true, "extra" to "dropped"))
        assertTrue(result is ZodResult.Success)
        assertEquals(false, result.data.containsKey("extra"))
    }

    @Test
    fun `strict should reject extra keys`() {
        val schema = baseObject().strict()
        assertTrue(schema.safeParse(mapOf("name" to "John", "age" to 30, "active" to true, "extra" to "x")) is ZodResult.Failure)
        assertTrue(schema.safeParse(mapOf("name" to "John", "age" to 30, "active" to true)) is ZodResult.Success)
    }
}
