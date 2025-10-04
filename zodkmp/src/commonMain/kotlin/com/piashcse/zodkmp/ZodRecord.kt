package com.piashcse.zodkmp

/**
 * Schema for validating records (objects with string keys and values of a specific type)
 */
class ZodRecord<T> private constructor(
    private val valueSchema: ZodSchema<T>,
    private val validations: List<(Map<String, T>) -> ZodError?>
) : ZodSchema<Map<String, T>> {
    companion object {
        fun <T> schema(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord(valueSchema, emptyList())
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