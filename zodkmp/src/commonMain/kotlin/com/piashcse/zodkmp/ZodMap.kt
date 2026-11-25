package com.piashcse.zodkmp

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Schema for validating maps. Accepts a [Map] and validates keys with [keySchema] and
 * values with [valueSchema], returning a [Map].
 */
public class ZodMap<K, V> private constructor(
    private val keySchema: ZodSchema<K>,
    private val valueSchema: ZodSchema<V>,
    private val validations: List<(Map<K, V>) -> ZodIssue?>,
    private val hints: Map<String, JsonElement> = emptyMap(),
) : ZodSchema<Map<K, V>> {
    public companion object {
        public fun <K, V> schema(
            keySchema: ZodSchema<K>,
            valueSchema: ZodSchema<V>,
        ): ZodMap<K, V> = ZodMap(keySchema, valueSchema, emptyList(), emptyMap())
    }

    private fun withValidation(
        newHints: Map<String, JsonElement> = emptyMap(),
        validation: (Map<K, V>) -> ZodIssue?,
    ): ZodMap<K, V> = ZodMap(keySchema, valueSchema, validations + validation, hints + newHints)

    internal fun keySchemaRef(): ZodSchema<K> = keySchema

    internal fun valueSchemaRef(): ZodSchema<V> = valueSchema

    internal fun jsonHints(): Map<String, JsonElement> = hints

    public fun min(
        minSize: Int,
        message: String = "Map must contain at least $minSize entry/entries",
    ): ZodMap<K, V> =
        withValidation(mapOf("minProperties" to JsonPrimitive(minSize))) { value ->
            if (value.size < minSize) issue(ZodIssueCode.TOO_SMALL, message) else null
        }

    public fun max(
        maxSize: Int,
        message: String = "Map must contain at most $maxSize entry/entries",
    ): ZodMap<K, V> =
        withValidation(mapOf("maxProperties" to JsonPrimitive(maxSize))) { value ->
            if (value.size > maxSize) issue(ZodIssueCode.TOO_BIG, message) else null
        }

    override fun safeParse(input: Any?): ZodResult<Map<K, V>> {
        val mapInput =
            input as? Map<*, *>
                ?: return ZodResult.Failure(typeError("map", input))

        val parsedEntries = mutableMapOf<K, V>()
        val issues = mutableListOf<ZodIssue>()

        for ((rawKey, rawValue) in mapInput) {
            val keyResult = keySchema.safeParse(rawKey)
            val valueResult = valueSchema.safeParse(rawValue)

            if (keyResult is ZodResult.Failure) {
                issues.add(
                    ZodIssue(
                        ZodIssueCode.INVALID_KEY,
                        "Invalid key: ${keyResult.error.errors.joinToString(", ")}",
                    ),
                )
            }
            if (valueResult is ZodResult.Failure) {
                issues.addAll(
                    valueResult.error.issues.prepend(ZodPathSegment.Property(rawKey.toString())),
                )
            }

            if (keyResult is ZodResult.Success && valueResult is ZodResult.Success) {
                parsedEntries[keyResult.data] = valueResult.data
            }
        }

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        return applyValidations(parsedEntries)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<Map<K, V>> {
        val mapInput =
            input as? Map<*, *>
                ?: return ZodResult.Failure(typeError("map", input))

        val parsedEntries = mutableMapOf<K, V>()
        val issues = mutableListOf<ZodIssue>()

        for ((rawKey, rawValue) in mapInput) {
            val keyResult = keySchema.safeParseSuspend(rawKey)
            val valueResult = valueSchema.safeParseSuspend(rawValue)

            if (keyResult is ZodResult.Failure) {
                issues.add(
                    ZodIssue(
                        ZodIssueCode.INVALID_KEY,
                        "Invalid key: ${keyResult.error.errors.joinToString(", ")}",
                    ),
                )
            }
            if (valueResult is ZodResult.Failure) {
                issues.addAll(
                    valueResult.error.issues.prepend(ZodPathSegment.Property(rawKey.toString())),
                )
            }

            if (keyResult is ZodResult.Success && valueResult is ZodResult.Success) {
                parsedEntries[keyResult.data] = valueResult.data
            }
        }

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        return applyValidations(parsedEntries)
    }

    private fun applyValidations(entries: Map<K, V>): ZodResult<Map<K, V>> {
        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(entries)?.let { issues.add(it) }
        }
        return if (issues.isEmpty()) ZodResult.Success(entries) else ZodResult.Failure(ZodError(issues))
    }
}
