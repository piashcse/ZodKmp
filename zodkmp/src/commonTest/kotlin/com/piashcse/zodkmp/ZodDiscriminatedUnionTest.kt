package com.piashcse.zodkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

sealed class Animal {
    data class Dog(val name: String, val goodBoy: Boolean) : Animal()
    data class Cat(val name: String, val lives: Double) : Animal()
}

class ZodDiscriminatedUnionTest {

    private fun dogSchema(): ZodObjectSchema<Animal.Dog> {
        val builder = ZodObjectShapeBuilder()
        builder.field("type", Zod.literal("dog"))
        builder.string("name")
        builder.boolean("goodBoy")
        return ZodObjectSchema.create(shape = builder.shape, parser = { map ->
            Animal.Dog(map["name"] as String, map["goodBoy"] as Boolean)
        })
    }

    private fun catSchema(): ZodObjectSchema<Animal.Cat> {
        val builder = ZodObjectShapeBuilder()
        builder.field("type", Zod.literal("cat"))
        builder.string("name")
        builder.number("lives")
        return ZodObjectSchema.create(shape = builder.shape, parser = { map ->
            Animal.Cat(map["name"] as String, map["lives"] as Double)
        })
    }

    @Test
    fun `discriminatedUnion should parse dog input into dog type`() {
        val schema = Zod.discriminatedUnion<Animal>(
            discriminator = "type",
            dogSchema(), catSchema()
        )

        val result = schema.safeParse(mapOf("type" to "dog", "name" to "Rex", "goodBoy" to true))
        assertTrue(result is ZodResult.Success)
        assertEquals(Animal.Dog("Rex", true), result.data)
    }

    @Test
    fun `discriminatedUnion should parse cat input into cat type`() {
        val schema = Zod.discriminatedUnion<Animal>(
            discriminator = "type",
            listOf(dogSchema(), catSchema())
        )

        val result = schema.safeParse(mapOf("type" to "cat", "name" to "Milo", "lives" to 9.0))
        assertTrue(result is ZodResult.Success)
        assertEquals(Animal.Cat("Milo", 9.0), result.data)
    }

    @Test
    fun `discriminatedUnion should reject unknown discriminator values`() {
        val schema = Zod.discriminatedUnion<Animal>(
            discriminator = "type",
            dogSchema(), catSchema()
        )

        val result = schema.safeParse(mapOf("type" to "bird", "name" to "Tweety"))
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `discriminatedUnion should reject missing discriminator key`() {
        val schema = Zod.discriminatedUnion<Animal>(
            discriminator = "type",
            dogSchema(), catSchema()
        )

        val result = schema.safeParse(mapOf("name" to "Rex"))
        assertTrue(result is ZodResult.Failure)
    }

    @Test
    fun `discriminatedUnion should fail when matched option is invalid`() {
        val schema = Zod.discriminatedUnion<Animal>(
            discriminator = "type",
            dogSchema(), catSchema()
        )

        val result = schema.safeParse(mapOf("type" to "dog", "name" to "Rex", "goodBoy" to "not-boolean"))
        assertTrue(result is ZodResult.Failure)
    }
}
