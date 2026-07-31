package com.piashcse.zodkmp

/**
 * Schema for validating maps. Accepts a [Map] and validates keys with [keySchema] and
 * values with [valueSchema], returning a [Map].
 */
class ZodMap<K, V> private constructor(
    private val keySchema: ZodSchema<K>,
    private val valueSchema: ZodSchema<V>,
    private val validations: List<(Map<K, V>) -> ZodError?>
) : ZodSchema<Map<K, V>> {
    companion object {
        fun <K, V> schema(keySchema: ZodSchema<K>, valueSchema: ZodSchema<V>): ZodMap<K, V> 
            = ZodMap(keySchema, valueSchema, emptyList())
    }
    
    fun min(minSize: Int, message: String = "Map must contain at least $minSize entry/entries"): ZodMap<K, V> {
        val validation: (Map<K, V>) -> ZodError? = { value ->
            if (value.size < minSize) ZodError(message) else null
        }
        return ZodMap(keySchema, valueSchema, validations + validation)
    }
    
    fun max(maxSize: Int, message: String = "Map must contain at most $maxSize entry/entries"): ZodMap<K, V> {
        val validation: (Map<K, V>) -> ZodError? = { value ->
            if (value.size > maxSize) ZodError(message) else null
        }
        return ZodMap(keySchema, valueSchema, validations + validation)
    }
    
    override fun parse(input: Any?): Map<K, V> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Map<K, V>> {
        val mapInput = input as? Map<*, *>
        if (mapInput == null) {
            return ZodResult.Failure(ZodError("Expected map, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val parsedEntries = mutableMapOf<K, V>()
        val errors = mutableListOf<String>()
        
        for ((rawKey, rawValue) in mapInput) {
            val keyResult = keySchema.safeParse(rawKey)
            val valueResult = valueSchema.safeParse(rawValue)
            
            when {
                keyResult is ZodResult.Failure -> {
                    keyResult.error.errors.forEach { error -> errors.add("key $rawKey: $error") }
                }
                valueResult is ZodResult.Failure -> {
                    valueResult.error.errors.forEach { error -> errors.add("value for '$rawKey': $error") }
                }
                keyResult is ZodResult.Success && valueResult is ZodResult.Success -> {
                    parsedEntries[keyResult.data] = valueResult.data
                }
            }
        }
        
        if (errors.isNotEmpty()) {
            return ZodResult.Failure(ZodError(errors))
        }
        
        for (validation in validations) {
            val error = validation(parsedEntries)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(parsedEntries)
    }
}
