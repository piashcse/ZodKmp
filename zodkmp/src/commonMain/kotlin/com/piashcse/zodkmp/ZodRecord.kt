package com.piashcse.zodkmp

/**
 * Schema for validating records (objects with string keys and values of a specific type).
 *
 * Behaviour depends on construction:
 * - [schema] with only a value schema validates every value, passing all keys through.
 * - [schema]/[strictSchema] with a key schema validates every key against the key schema
 *   and every value against the value schema; non-matching keys are an error.
 * - [looseSchema] with a key schema validates every value and validates keys that match the
 *   key schema; keys that do not match the key schema never produce errors.
 */
class ZodRecord<T> private constructor(
    private val keySchema: ZodSchema<String>?,
    private val valueSchema: ZodSchema<T>,
    private val strict: Boolean
) : ZodSchema<Map<String, T>> {
    companion object {
        fun <T> schema(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(null, valueSchema, false)
        fun <T> schema(keySchema: ZodSchema<String>, valueSchema: ZodSchema<T>): ZodRecord<T> =
            ZodRecord(keySchema, valueSchema, true)
        fun <T> looseSchema(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(null, valueSchema, false)
        fun <T> looseSchema(keySchema: ZodSchema<String>, valueSchema: ZodSchema<T>): ZodRecord<T> =
            ZodRecord(keySchema, valueSchema, false)
        fun <T> strictSchema(keySchema: ZodSchema<String>, valueSchema: ZodSchema<T>): ZodRecord<T> =
            ZodRecord(keySchema, valueSchema, true)
    }

    override fun safeParse(input: Any?): ZodResult<Map<String, T>> {
        val mapInput = input as? Map<*, *>
            ?: return ZodResult.Failure(typeError("record (object)", input))

        val nonStringKey = mapInput.keys.firstOrNull { it !is String }
        if (nonStringKey != null) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_KEY,
                        "Expected record with string keys, received key of type ${nonStringKey.typeName()}"
                    )
                )
            )
        }

        @Suppress("UNCHECKED_CAST")
        val stringKeyMap = mapInput as Map<String, Any?>
        val parsedValues = mutableMapOf<String, T>()
        val issues = mutableListOf<ZodIssue>()

        for ((key, value) in stringKeyMap) {
            if (keySchema != null && keySchema.safeParse(key) is ZodResult.Failure) {
                if (strict) {
                    issues.add(
                        ZodIssue(
                            ZodIssueCode.INVALID_KEY,
                            "Invalid key",
                            path = listOf(ZodPathSegment.Property(key))
                        )
                    )
                    continue
                }
            }

            when (val valueResult = valueSchema.safeParse(value)) {
                is ZodResult.Success -> parsedValues[key] = valueResult.data
                is ZodResult.Failure -> issues.addAll(
                    valueResult.error.issues.prepend(ZodPathSegment.Property(key))
                )
            }
        }

        return if (issues.isEmpty()) ZodResult.Success(parsedValues) else ZodResult.Failure(ZodError(issues))
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<Map<String, T>> {
        val mapInput = input as? Map<*, *>
            ?: return ZodResult.Failure(typeError("record (object)", input))

        val nonStringKey = mapInput.keys.firstOrNull { it !is String }
        if (nonStringKey != null) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_KEY,
                        "Expected record with string keys, received key of type ${nonStringKey.typeName()}"
                    )
                )
            )
        }

        @Suppress("UNCHECKED_CAST")
        val stringKeyMap = mapInput as Map<String, Any?>
        val parsedValues = mutableMapOf<String, T>()
        val issues = mutableListOf<ZodIssue>()

        for ((key, value) in stringKeyMap) {
            if (keySchema != null && keySchema.safeParseSuspend(key) is ZodResult.Failure) {
                if (strict) {
                    issues.add(
                        ZodIssue(
                            ZodIssueCode.INVALID_KEY,
                            "Invalid key",
                            path = listOf(ZodPathSegment.Property(key))
                        )
                    )
                    continue
                }
            }

            when (val valueResult = valueSchema.safeParseSuspend(value)) {
                is ZodResult.Success -> parsedValues[key] = valueResult.data
                is ZodResult.Failure -> issues.addAll(
                    valueResult.error.issues.prepend(ZodPathSegment.Property(key))
                )
            }
        }

        return if (issues.isEmpty()) ZodResult.Success(parsedValues) else ZodResult.Failure(ZodError(issues))
    }
}
