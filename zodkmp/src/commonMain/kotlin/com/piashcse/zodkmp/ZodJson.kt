package com.piashcse.zodkmp

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Converts a [JsonElement] into the plain Kotlin values expected by ZodKmp schemas.
 *
 * Objects become `Map<String, Any?>`, arrays become `List<Any?>`, strings stay strings and
 * unquoted numbers/booleans become [Double]/[Boolean].
 */
public fun JsonElement.toZodValue(): Any? =
    when (this) {
        is JsonNull -> null
        is JsonPrimitive ->
            when {
                isString -> content
                booleanOrNull != null -> booleanOrNull == true
                else -> content.toDoubleOrNull() ?: content
            }
        is JsonObject -> entries.associate { (key, value) -> key to value.toZodValue() }
        is JsonArray -> map { it.toZodValue() }
    }

public fun <T> ZodSchema<T>.parseJson(json: JsonElement): T = parse(json.toZodValue())

public fun <T> ZodSchema<T>.safeParseJson(json: JsonElement): ZodResult<T> = safeParse(json.toZodValue())

public suspend fun <T> ZodSchema<T>.safeParseJsonSuspend(json: JsonElement): ZodResult<T> = safeParseSuspend(json.toZodValue())

public suspend fun <T> ZodSchema<T>.parseJsonSuspend(json: JsonElement): T = parseSuspend(json.toZodValue())
