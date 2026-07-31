package com.piashcse.zodkmp

/**
 * Schema for validating records (objects with string keys and values of a specific type).
 *
 * Behaviour depends on construction:
 * - [schema] with only a value schema validates every value, passing all keys through.
 * - [schema]/[strictSchema] with a key schema validates every key against the key schema
 *   and every value against the value schema; non-matching keys are an error.
 * - [looseSchema] with a key schema validates every value and validates keys that match the
 *   key schema; keys that do not match the key schema never produce errors. All values are
 *   validated, so the output remains fully typed ([Map]&lt;String, T&gt;).
 */
class ZodRecord<T> private constructor(
    private val keySchema: ZodSchema<String>?,
    private val valueSchema: ZodSchema<T>,
    private val strict: Boolean,
    private val validations: List<(Map<String, T>) -> ZodError?>
) : ZodSchema<Map<String, T>> {
    companion object {
        fun <T> schema(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(null, valueSchema, false, emptyList())
        fun <T> schema(keySchema: ZodSchema<String>, valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(keySchema, valueSchema, true, emptyList())
        fun <T> looseSchema(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(null, valueSchema, false, emptyList())
        fun <T> looseSchema(keySchema: ZodSchema<String>, valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(keySchema, valueSchema, false, emptyList())
        fun <T> strictSchema(keySchema: ZodSchema<String>, valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(keySchema, valueSchema, true, emptyList())
    }
    
    override fun parse(input: Any?): Map<String, T> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Map<String, T>> {
        val mapInput = input as? Map<*, *>
        if (mapInput == null) {
            return ZodResult.Failure(ZodError("Expected record (object), received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val stringKeyMap = mapInput.mapKeys { it.key.toString() }
        val parsedValues = mutableMapOf<String, T>()
        val errors = mutableListOf<String>()
        
        for ((key, value) in stringKeyMap) {
            val keyMatches = keySchema?.safeParse(key) is ZodResult.Success
            if (keySchema != null && !keyMatches) {
                if (strict) {
                    errors.add("key '$key': invalid key")
                    continue
                }
                // Loose record: non-matching keys never error; only their values are validated.
            }
            
            val valueResult = valueSchema.safeParse(value)
            when (valueResult) {
                is ZodResult.Success -> parsedValues[key] = valueResult.data
                is ZodResult.Failure -> {
                    valueResult.error.errors.forEach { error ->
                        errors.add("$key: $error")
                    }
                }
            }
        }
        
        if (errors.isNotEmpty()) {
            return ZodResult.Failure(ZodError(errors))
        }
        
        // Apply record-level validations
        for (validation in validations) {
            val error = validation(parsedValues)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(parsedValues)
    }
}
