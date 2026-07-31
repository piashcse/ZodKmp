package com.piashcse.zodkmp

/**
 * Schema for validating enum values
 */
class ZodEnum<T> private constructor(
    private val values: Set<T>,
    private val coerceByEnumName: Boolean,
    private val validations: List<(T) -> ZodError?>
) : ZodSchema<T> {
    companion object {
        fun <T> schema(vararg values: T): ZodEnum<T> = ZodEnum(values.toSet(), false, emptyList())
        fun <T> schema(values: Collection<T>): ZodEnum<T> = ZodEnum(values.toSet(), false, emptyList())
        fun <T> schema(values: Collection<T>, coerceByEnumName: Boolean): ZodEnum<T> = ZodEnum(values.toSet(), coerceByEnumName, emptyList())
    }
    
    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        val matched = when {
            values.contains(input) -> input as T
            coerceByEnumName && input is String -> {
                @Suppress("UNCHECKED_CAST")
                values.firstOrNull { (it as Enum<*>).name == input } as T?
            }
            else -> null
        }
        
        if (matched == null) {
            return ZodResult.Failure(ZodError("Expected enum value, received $input. Valid values: ${values.joinToString(", ")}"))
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(matched)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(matched)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}