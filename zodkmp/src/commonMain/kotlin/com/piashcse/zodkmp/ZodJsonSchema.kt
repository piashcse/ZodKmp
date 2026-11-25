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
 * represented by their input schema. Recursive `Zod.lazy` schemas produce `{}` because no
 * `$ref` graph is generated.
 */
public fun ZodSchema<*>.toJsonSchema(): JsonObject = toJsonSchemaElement() as? JsonObject ?: buildJsonObject { }

internal fun ZodSchema<*>.toJsonSchemaElement(): JsonElement = when (this) {
    is ZodString ->
        buildJsonObject {
            put("type", JsonPrimitive("string"))
            putHints(jsonHints())
        }
    is ZodNumber -> {
        val integer = jsonHints()["integer"]?.let { it is JsonPrimitive && it.booleanOrNull == true } == true
        buildJsonObject {
            put("type", JsonPrimitive(if (integer) "integer" else "number"))
            putHints(jsonHints().filterKeys { it != "integer" })
        }
    }
    is ZodBoolean -> buildJsonObject { put("type", JsonPrimitive("boolean")) }
    is ZodDate ->
        buildJsonObject {
            put("type", JsonPrimitive("string"))
            put("format", JsonPrimitive("date-time"))
        }
    is ZodNull -> buildJsonObject { put("type", JsonPrimitive("null")) }
    is ZodAny, is ZodUnknown, is ZodUndefined, is ZodVoid, is ZodInstanceOf<*>, is ZodLazy<*> -> buildJsonObject { }
    is ZodNever -> buildJsonObject { put("not", buildJsonObject { }) }
    is ZodLiteral<*> -> buildJsonObject { put("const", literalValue.toJsonElement()) }
    is ZodEnum<*> ->
        buildJsonObject {
            put("enum", JsonArray(enumValues().map { it.toJsonElement() }))
        }
    is ZodArray<*> ->
        buildJsonObject {
            put("type", JsonPrimitive("array"))
            put("items", elementSchema.toJsonSchemaElement())
            putHints(jsonHints())
        }
    is ZodSet<*> ->
        buildJsonObject {
            put("type", JsonPrimitive("array"))
            put("items", elementSchema.toJsonSchemaElement())
            put("uniqueItems", JsonPrimitive(true))
            putHints(jsonHints())
        }
    is ZodTuple ->
        buildJsonObject {
            put("type", JsonPrimitive("array"))
            put("items", JsonArray(elements().map { it.toJsonSchemaElement() }))
            put("minItems", JsonPrimitive(elements().size))
            put("maxItems", JsonPrimitive(elements().size))
        }
    is ZodMap<*, *> ->
        buildJsonObject {
            put("type", JsonPrimitive("object"))
            put("additionalProperties", valueSchemaRef().toJsonSchemaElement())
            putHints(jsonHints())
        }
    is ZodRecord<*> ->
        buildJsonObject {
            put("type", JsonPrimitive("object"))
            put("additionalProperties", value().toJsonSchemaElement())
        }
    is ZodObjectSchema<*> -> toJsonSchemaObject()
    is ZodUnion ->
        buildJsonObject {
            put("anyOf", JsonArray(options().map { it.toJsonSchemaElement() }))
        }
    is ZodIntersection<*, *> ->
        buildJsonObject {
            put("allOf", JsonArray(listOf(left().toJsonSchemaElement(), right().toJsonSchemaElement())))
        }
    is ZodDiscriminatedUnion<*> ->
        buildJsonObject {
            put("oneOf", JsonArray(optionSchemas().map { it.toJsonSchemaElement() }))
        }
    is ZodOptional<*> -> wrapped().toJsonSchemaElement()
    is ZodNullable<*> ->
        buildJsonObject {
            put(
                "anyOf",
                JsonArray(
                    listOf(
                        wrapped().toJsonSchemaElement(),
                        buildJsonObject { put("type", JsonPrimitive("null")) },
                    ),
                ),
            )
        }
    is ZodDefault<*> -> wrapped().toJsonSchemaElement()
    is ZodCatch<*> -> wrapped().toJsonSchemaElement()
    is ZodTransform<*, *> -> inputSchema().toJsonSchemaElement()
    is ZodPipe<*, *> -> firstSchema().toJsonSchemaElement()
    is ZodPreprocess<*> -> wrapped().toJsonSchemaElement()
    is ZodSchemaWithRefinement<*> -> base().toJsonSchemaElement()
    is ZodDescribe<*> -> schema.toJsonSchemaElement()
    else -> buildJsonObject { }
}

private fun ZodObjectSchema<*>.toJsonSchemaObject(): JsonObject {
    val properties = shape.mapValues { (_, schema) -> schema.toJsonSchemaElement() }
    val required = shape.filterValues { schema -> !schema.acceptsMissing() }.keys
    return buildJsonObject {
        put("type", JsonPrimitive("object"))
        put("properties", JsonObject(properties))
        if (required.isNotEmpty()) put("required", JsonArray(required.map { JsonPrimitive(it) }))
        when {
            isStrict() -> put("additionalProperties", JsonPrimitive(false))
            isPassthrough() -> put("additionalProperties", JsonPrimitive(true))
        }
    }
}

private fun ZodSchema<*>.acceptsMissing(): Boolean = when (this) {
    is ZodOptional<*>, is ZodDefault<*>, is ZodCatch<*>, is ZodAny, is ZodUnknown, is ZodUndefined, is ZodVoid -> true
    else -> false
}

private fun JsonObjectBuilder.putHints(hints: Map<String, JsonElement>) {
    for ((key, value) in hints) put(key, value)
}

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Char -> JsonPrimitive(this.toString())
    else -> JsonPrimitive(toString())
}
