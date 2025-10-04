package com.piashcse.zodkmp

/**
 * Schema for validating literal values
 */
class ZodLiteral<T> private constructor(
    private val value: T,
    private val validations: List<(T) -> ZodError?>
) : ZodSchema<T> {
    companion object {
        fun <T> schema(value: T): ZodLiteral<T> = ZodLiteral(value, emptyList())
    }
    
    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        if (input != value) {
            return ZodResult.Failure(ZodError("Expected literal ${value}, received $input"))
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