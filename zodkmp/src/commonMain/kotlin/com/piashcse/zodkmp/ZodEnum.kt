package com.piashcse.zodkmp

/**
 * Schema for validating enum values
 */
class ZodEnum<T> private constructor(
    private val values: Set<T>,
    private val validations: List<(T) -> ZodError?>
) : ZodSchema<T> {
    companion object {
        fun <T> schema(vararg values: T): ZodEnum<T> = ZodEnum(values.toSet(), emptyList())
        fun <T> schema(values: Collection<T>): ZodEnum<T> = ZodEnum(values.toSet(), emptyList())
    }
    
    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        if (!values.contains(input)) {
            return ZodResult.Failure(ZodError("Expected enum value, received $input. Valid values: ${values.joinToString(", ")}"))
        }
        
        @Suppress("UNCHECKED_CAST")
        val typedInput = input as T
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(typedInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(typedInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}