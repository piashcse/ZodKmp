package com.piashcse.zodkmp

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodJsonSchemaTest {

    @Test
    fun `string constraints become json schema keywords`() {
        val schema = Zod.string().min(2).max(10).email()
        val json = schema.toJsonSchema()

        assertEquals("string", json["type"]!!.jsonPrimitive.content)
        assertEquals(2, json["minLength"]!!.jsonPrimitive.int)
        assertEquals(10, json["maxLength"]!!.jsonPrimitive.int)
        assertEquals("email", json["format"]!!.jsonPrimitive.content)
    }

    @Test
    fun `numbers, integers and multiples are described`() {
        val json = Zod.number().int().min(0.0).max(100.0).multipleOf(5.0).toJsonSchema()
        assertEquals("integer", json["type"]!!.jsonPrimitive.content)
        assertEquals("0.0", json["minimum"]!!.jsonPrimitive.content)
        assertEquals("100.0", json["maximum"]!!.jsonPrimitive.content)
        assertEquals("5.0", json["multipleOf"]!!.jsonPrimitive.content)
    }

    @Test
    fun `objects include properties required keys and additional properties policy`() {
        val schema = Zod.objectSchema<Map<String, Any?>>({
            string("name")
            optionalString("nickname")
            field("age", Zod.number().default(18.0))
        }) { it }.strict()

        val json = schema.toJsonSchema()
        assertEquals("object", json["type"]!!.jsonPrimitive.content)
        val properties = json["properties"]!!.jsonObject
        assertEquals("string", properties["name"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals(listOf("name"), (json["required"] as JsonArray).map { it.jsonPrimitive.content })
        assertEquals(false, json["additionalProperties"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `arrays sets tuples unions and nullables are described`() {
        val array = Zod.array(Zod.string()).min(1).unique().toJsonSchema()
        assertEquals("array", array["type"]!!.jsonPrimitive.content)
        assertEquals(1, array["minItems"]!!.jsonPrimitive.int)
        assertEquals(true, array["uniqueItems"]!!.jsonPrimitive.boolean)

        val set = Zod.set(Zod.number()).toJsonSchema()
        assertEquals(true, set["uniqueItems"]!!.jsonPrimitive.boolean)

        val tuple = Zod.tuple(Zod.string(), Zod.number()).toJsonSchema()
        assertEquals(2, (tuple["items"] as JsonArray).size)

        val union = Zod.union(Zod.string(), Zod.number()).toJsonSchema()
        assertEquals(2, (union["anyOf"] as JsonArray).size)

        val nullable = Zod.string().nullable().toJsonSchema()
        assertEquals(2, (nullable["anyOf"] as JsonArray).size)
    }

    @Test
    fun `literals and enums are described`() {
        assertEquals("admin", Zod.literal("admin").toJsonSchema()["const"]!!.jsonPrimitive.content)
        val enum = Zod.enum("a", "b").toJsonSchema()["enum"]!!.jsonArray
        assertEquals(listOf("a", "b"), enum.map { it.jsonPrimitive.content })
    }

    @Test
    fun `recursive lazy schemas do not recurse forever`() {
        lateinit var node: ZodSchema<Map<String, Any?>>
        node = Zod.lazy {
            Zod.objectSchema<Map<String, Any?>>({
                string("name")
                field("children", Zod.array(node))
            }) { it }
        }
        val json = node.toJsonSchema() as JsonObject
        assertTrue(json.isEmpty())
    }
}
