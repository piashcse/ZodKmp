package com.piashcse.zodkmp

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject

/**
 * Converts a schema to a JSON Schema (draft 2020-12 style) object.
 *
 * The conversion is structural: types, object properties/required keys, array items,
 * unions, literals, enums and the string/number/collection constraints that have a JSON
 * Schema equivalent are emitted. Transforms, refinements, `catch` and preprocessors are
 * represented by their input schema.
 *
 * Recursive [ZodLazy] schemas are emitted as `$ref`/`$defs` entries.
 */
public fun ZodSchema<*>.toJsonSchema(): JsonObject {
    val builder = JsonSchemaBuilder()
    val root = builder.build(this) as? JsonObject ?: buildJsonObject { }
    val definitions = builder.definitions()
    if (definitions.isEmpty()) return root
    return JsonObject(root + ("\$defs" to JsonObject(definitions)))
}

private class JsonSchemaBuilder {
    private val definitions = linkedMapOf<String, JsonObject>()
    private val lazyNames = mutableMapOf<ZodLazy<*>, String>()
    private val building = mutableSetOf<ZodLazy<*>>()
    private var counter = 0

    fun definitions(): Map<String, JsonObject> = definitions

    fun build(schema: ZodSchema<*>): JsonElement =
        when (schema) {
            is ZodString ->
                buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    putHints(schema.jsonHints())
                }
            is ZodNumber -> {
                val integer = schema.jsonHints()["integer"]?.let { it is JsonPrimitive && it.booleanOrNull == true } == true
                buildJsonObject {
                    put("type", JsonPrimitive(if (integer) "integer" else "number"))
                    putHints(schema.jsonHints().filterKeys { it != "integer" })
                }
            }
            is ZodBoolean -> buildJsonObject { put("type", JsonPrimitive("boolean")) }
            is ZodDate ->
                buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put("format", JsonPrimitive("date-time"))
                }
            is ZodNull -> buildJsonObject { put("type", JsonPrimitive("null")) }
            is ZodAny, is ZodUnknown, is ZodUndefined, is ZodVoid, is ZodInstanceOf<*> -> buildJsonObject { }
            is ZodNever -> buildJsonObject { put("not", buildJsonObject { }) }
            is ZodLiteral<*> -> buildJsonObject { put("const", (schema as ZodLiteral<*>).literalValue.toJsonElement()) }
            is ZodEnum<*> -> {
                val values = (schema as ZodEnum<*>).enumValues().map { it.toJsonElement() }
                buildJsonObject { put("enum", JsonArray(values)) }
            }
            is ZodArray<*> ->
                buildJsonObject {
                    put("type", JsonPrimitive("array"))
                    put("items", build((schema as ZodArray<*>).elementSchema))
                    putHints(schema.jsonHints())
                }
            is ZodSet<*> ->
                buildJsonObject {
                    put("type", JsonPrimitive("array"))
                    put("items", build((schema as ZodSet<*>).elementSchema))
                    put("uniqueItems", JsonPrimitive(true))
                    putHints(schema.jsonHints())
                }
            is ZodTuple ->
                buildJsonObject {
                    put("type", JsonPrimitive("array"))
                    put("items", JsonArray((schema as ZodTuple).elements().map { build(it) }))
                    put("minItems", JsonPrimitive((schema as ZodTuple).elements().size))
                    put("maxItems", JsonPrimitive((schema as ZodTuple).elements().size))
                }
            is ZodMap<*, *> ->
                buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("additionalProperties", build((schema as ZodMap<*, *>).valueSchemaRef()))
                    putHints(schema.jsonHints())
                }
            is ZodRecord<*> ->
                buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("additionalProperties", build((schema as ZodRecord<*>).value()))
                }
            is ZodObjectSchema<*> -> objectSchema(schema)
            is ZodUnion -> buildJsonObject { put("anyOf", JsonArray((schema as ZodUnion).options().map { build(it) })) }
            is ZodIntersection<*, *> ->
                buildJsonObject {
                    put("allOf", JsonArray(listOf(build((schema as ZodIntersection<*, *>).left()), build((schema as ZodIntersection<*, *>).right()))))
                }
            is ZodDiscriminatedUnion<*> ->
                buildJsonObject {
                    put("oneOf", JsonArray((schema as ZodDiscriminatedUnion<*>).optionSchemas().map { build(it) }))
                }
            is ZodOptional<*> -> build((schema as ZodOptional<*>).wrapped())
            is ZodNullable<*> ->
                buildJsonObject {
                    put(
                        "anyOf",
                        JsonArray(
                            listOf(
                                build((schema as ZodNullable<*>).wrapped()),
                                buildJsonObject { put("type", JsonPrimitive("null")) },
                            ),
                        ),
                    )
                }
            is ZodDefault<*> -> build((schema as ZodDefault<*>).wrapped())
            is ZodCatch<*> -> build((schema as ZodCatch<*>).wrapped())
            is ZodTransform<*, *> -> build((schema as ZodTransform<*, *>).inputSchema())
            is ZodPipe<*, *> -> build((schema as ZodPipe<*, *>).firstSchema())
            is ZodPreprocess<*> -> build((schema as ZodPreprocess<*>).wrapped())
            is ZodSchemaWithRefinement<*> -> build((schema as ZodSchemaWithRefinement<*>).base())
            is ZodDescribe<*> -> build((schema as ZodDescribe<*>).schema)
            is ZodLazy<*> -> lazySchema(schema as ZodLazy<*>)
            else -> buildJsonObject { }
        }

    private fun objectSchema(schema: ZodObjectSchema<*>): JsonObject {
        val properties = schema.shape.mapValues { (_, property) -> build(property) }
        val required = schema.shape.filterValues { property -> !property.acceptsMissing() }.keys
        return buildJsonObject {
            put("type", JsonPrimitive("object"))
            put("properties", JsonObject(properties))
            if (required.isNotEmpty()) put("required", JsonArray(required.map { JsonPrimitive(it) }))
            when {
                schema.isStrict() -> put("additionalProperties", JsonPrimitive(false))
                schema.isPassthrough() -> put("additionalProperties", JsonPrimitive(true))
            }
        }
    }

    private fun lazySchema(schema: ZodLazy<*>): JsonElement {
        val name = lazyNames.getOrPut(schema) { "Lazy${counter++}" }
        if (schema in building || definitions.containsKey(name)) return reference(name)

        building += schema
        val resolved = build(schema.resolve()) as? JsonObject ?: buildJsonObject { }
        building -= schema
        definitions[name] = resolved
        return reference(name)
    }

    private fun reference(name: String): JsonObject = buildJsonObject { put("\$ref", JsonPrimitive("#/\$defs/$name")) }
}

private fun ZodSchema<*>.acceptsMissing(): Boolean =
    when (this) {
        is ZodOptional<*>, is ZodDefault<*>, is ZodCatch<*>, is ZodAny, is ZodUnknown, is ZodUndefined, is ZodVoid -> true
        else -> false
    }

private fun JsonObjectBuilder.putHints(hints: Map<String, JsonElement>) {
    for ((key, value) in hints) put(key, value)
}

private fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull
        is JsonElement -> this
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Char -> JsonPrimitive(this.toString())
        else -> JsonPrimitive(toString())
    }
